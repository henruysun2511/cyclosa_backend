package com.cyclosa.employee.service;

import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.request.*;
import com.cyclosa.employee.dto.response.*;
import com.cyclosa.employee.entity.*;
import com.cyclosa.employee.enums.EmployeeChangeType;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.mapper.EmployeeMapper;
import com.cyclosa.employee.repository.*;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.JobLevelSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.organization.service.GeographyService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.organization.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePersonalInfoRepository personalInfoRepository;
    private final EmployeeEmploymentInfoRepository employmentInfoRepository;
    private final EmployeeDependentRepository dependentRepository;
    private final EmployeeEmergencyContactRepository emergencyContactRepository;
    private final EmployeeHistoryRepository historyRepository;

    private final OrganizationalUnitService orgUnitService;
    private final GeographyService geographyService;
    private final PositionService positionService;
    private final CompanyService companyService;

    private final UserService userService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final EmployeeMapper employeeMapper;
    @Transactional(readOnly = true)
    public PageData<EmployeeResponse> getEmployees(UUID companyId, EmployeeFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new EmployeeFilter();
        }
        if (companyId != null) {
            filter.setCompanyId(companyId);
        }

        // Áp dụng bảo vệ dữ liệu theo DataScope
        applyDataScope(filter);

        Specification<Employee> spec = EmployeeSpecification.filter(filter);
        Page<Employee> page = employeeRepository.findAll(spec, pageable);

        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        // Pre-fetch nested summary objects to eliminate N+1 queries
        Set<UUID> companyIds = page.getContent().stream()
                .map(Employee::getCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, CompanySummary> companyMap = companyService.getCompanySummaries(companyIds);

        Set<UUID> unitIds = page.getContent().stream()
                .map(Employee::getEmploymentInfo)
                .filter(Objects::nonNull)
                .map(EmployeeEmploymentInfo::getOrganizationalUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, OrgUnitSummary> orgUnitMap = orgUnitService.getUnitSummaries(unitIds);

        Set<UUID> branchIds = page.getContent().stream()
                .map(Employee::getEmploymentInfo)
                .filter(Objects::nonNull)
                .map(EmployeeEmploymentInfo::getBranchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, BranchSummary> branchMap = geographyService.getBranchSummaries(branchIds);

        Set<UUID> positionIds = page.getContent().stream()
                .map(Employee::getEmploymentInfo)
                .filter(Objects::nonNull)
                .map(EmployeeEmploymentInfo::getPositionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, PositionSummary> positionMap = positionService.getPositionSummaries(positionIds);

        Set<UUID> managerIds = page.getContent().stream()
                .map(Employee::getEmploymentInfo)
                .filter(Objects::nonNull)
                .map(EmployeeEmploymentInfo::getManagerEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> managerMap = getEmployeeSummaries(managerIds);

        List<EmployeeResponse> responses = page.getContent().stream()
                .map(emp -> toSummaryResponse(emp, companyMap, orgUnitMap, branchMap, positionMap, managerMap))
                .toList();

        return PageData.of(page, responses);
    }
    @Transactional(readOnly = true)
    public EmployeeDetailResponse getEmployeeById(UUID companyId, UUID id) {
        Employee employee = findEmployee(companyId, id);
        validateAccess(employee);
        return toDetailResponse(employee);
    }
    @Transactional
    public EmployeeDetailResponse createEmployee(UUID companyId, CreateEmployeeRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        // 1. Kiểm tra / sinh mã nhân viên tự động
        String code = request.getEmployeeCode();
        if (code == null || code.isBlank()) {
            code = generateEmployeeCode(effectiveCompanyId);
        } else {
            if (employeeRepository.existsByEmployeeCodeAndCompanyId(code, effectiveCompanyId)) {
                throw new AppException(EmployeeErrorCode.EMPLOYEE_CODE_EXISTS);
            }
        }

        // 2. Kiểm tra trùng số CCCD
        if (personalInfoRepository.existsByNationalIdNumber(request.getNationalIdNumber())) {
            throw new AppException(EmployeeErrorCode.NATIONAL_ID_EXISTS);
        }

        // 3. Kiểm tra các khóa ngoại 3 chiều tổ chức
        validateOrganizationForeignKeys(effectiveCompanyId, request.getOrganizationalUnitId(),
                request.getBranchId(), request.getPositionId(), request.getJobLevelId());

        // 4. Tạo thực thể Employee trung tâm
        Employee employee = employeeMapper.toEntity(request);
        employee.setCompanyId(effectiveCompanyId);
        employee.setEmployeeCode(code);
        employee.setEmploymentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : EmploymentStatus.PROBATION);

        Employee savedEmployee = employeeRepository.save(employee);

        // 5. Tạo Personal Info
        EmployeePersonalInfo personalInfo = employeeMapper.toPersonalInfoEntity(request);
        personalInfo.setEmployee(savedEmployee);
        personalInfo = personalInfoRepository.save(personalInfo);
        savedEmployee.setPersonalInfo(personalInfo);

        // 6. Tạo Employment Info
        EmployeeEmploymentInfo employmentInfo = employeeMapper.toEmploymentInfoEntity(request);
        employmentInfo.setEmployee(savedEmployee);
        employmentInfo = employmentInfoRepository.save(employmentInfo);
        savedEmployee.setEmploymentInfo(employmentInfo);

        // 7. Xử lý liên kết hoặc tạo tài khoản User
        handleUserAccountLinking(savedEmployee, request);

        // 8. Ghi nhận lịch sử tiếp nhận nhân sự (Audit Trail)
        recordHistory(savedEmployee, EmployeeChangeType.HIRE, null,
                "Tiếp nhận nhân sự mới: " + savedEmployee.getFullName() + " (" + savedEmployee.getEmployeeCode() + ")",
                request.getHireDate(), "Tiếp nhận nhân sự mới vào hệ thống");

        log.info("Created Employee id={}, code={}, companyId={}", savedEmployee.getId(), savedEmployee.getEmployeeCode(), effectiveCompanyId);
        return toDetailResponse(savedEmployee);
    }
    @Transactional
    public EmployeeDetailResponse updatePersonalInfo(UUID companyId, UUID id, UpdatePersonalInfoRequest request) {
        Employee employee = findEmployee(companyId, id);
        validateAccess(employee);

        EmployeePersonalInfo info = employee.getPersonalInfo();
        if (info == null) {
            info = EmployeePersonalInfo.builder().employee(employee).build();
        }

        // Kiểm tra trùng CCCD nếu thay đổi sang số mới
        if (request.getNationalIdNumber() != null && !request.getNationalIdNumber().equals(info.getNationalIdNumber())) {
            if (personalInfoRepository.existsByNationalIdNumber(request.getNationalIdNumber())) {
                throw new AppException(EmployeeErrorCode.NATIONAL_ID_EXISTS);
            }
            info.setNationalIdNumber(request.getNationalIdNumber());
        }

        employee.setFullName(request.getFullName());
        info.setGender(request.getGender());
        info.setDateOfBirth(request.getDateOfBirth());
        info.setNationalIdIssueDate(request.getNationalIdIssueDate());
        info.setNationalIdIssuePlace(request.getNationalIdIssuePlace());
        info.setTaxCode(request.getTaxCode());
        info.setSocialInsuranceNumber(request.getSocialInsuranceNumber());
        info.setBankAccountNumber(request.getBankAccountNumber());
        info.setBankName(request.getBankName());
        info.setBankBranch(request.getBankBranch());
        info.setMaritalStatus(request.getMaritalStatus());
        info.setNationality(request.getNationality() != null ? request.getNationality() : "Việt Nam");
        info.setPersonalEmail(request.getPersonalEmail());
        info.setPhone(request.getPhone());
        info.setPermanentAddress(request.getPermanentAddress());
        info.setCurrentAddress(request.getCurrentAddress());
        info.setPhotoUrl(request.getPhotoUrl());

        personalInfoRepository.save(info);
        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("Updated personal info for Employee id={}", id);
        return toDetailResponse(updatedEmployee);
    }
    @Transactional
    public EmployeeDetailResponse updateEmploymentInfo(UUID companyId, UUID id, UpdateEmploymentInfoRequest request) {
        Employee employee = findEmployee(companyId, id);
        validateAccess(employee);

        if (request.getManagerEmployeeId() != null && request.getManagerEmployeeId().equals(id)) {
            throw new AppException(EmployeeErrorCode.SELF_MANAGER_NOT_ALLOWED);
        }

        validateOrganizationForeignKeys(employee.getCompanyId(), request.getOrganizationalUnitId(),
                request.getBranchId(), request.getPositionId(), request.getJobLevelId());

        EmployeeEmploymentInfo info = employee.getEmploymentInfo();
        if (info == null) {
            info = EmployeeEmploymentInfo.builder().employee(employee).build();
        }

        // Ghi nhận biến động công tác nếu có thay đổi
        LocalDate effectiveDate = LocalDate.now();
        String reason = (request.getChangeReason() != null && !request.getChangeReason().isBlank())
                ? request.getChangeReason()
                : "Điều chuyển / Bổ nhiệm công tác";

        if (!Objects.equals(info.getOrganizationalUnitId(), request.getOrganizationalUnitId())) {
            recordHistory(employee, EmployeeChangeType.DEPARTMENT_CHANGE,
                    String.valueOf(info.getOrganizationalUnitId()),
                    String.valueOf(request.getOrganizationalUnitId()),
                    effectiveDate, reason);
            info.setOrganizationalUnitId(request.getOrganizationalUnitId());
        }

        if (!Objects.equals(info.getPositionId(), request.getPositionId())) {
            recordHistory(employee, EmployeeChangeType.POSITION_CHANGE,
                    String.valueOf(info.getPositionId()),
                    String.valueOf(request.getPositionId()),
                    effectiveDate, reason);
            info.setPositionId(request.getPositionId());
        }

        if (!Objects.equals(info.getBranchId(), request.getBranchId())) {
            recordHistory(employee, EmployeeChangeType.BRANCH_CHANGE,
                    String.valueOf(info.getBranchId()),
                    String.valueOf(request.getBranchId()),
                    effectiveDate, reason);
            info.setBranchId(request.getBranchId());
        }

        if (!Objects.equals(info.getManagerEmployeeId(), request.getManagerEmployeeId())) {
            recordHistory(employee, EmployeeChangeType.MANAGER_CHANGE,
                    String.valueOf(info.getManagerEmployeeId()),
                    String.valueOf(request.getManagerEmployeeId()),
                    effectiveDate, reason);
            info.setManagerEmployeeId(request.getManagerEmployeeId());
        }

        info.setJobLevelId(request.getJobLevelId());
        if (request.getEmploymentType() != null) {
            info.setEmploymentType(request.getEmploymentType());
        }
        info.setCompanyEmail(request.getCompanyEmail());
        info.setWorkLocation(request.getWorkLocation());
        info.setProbationEndDate(request.getProbationEndDate());

        employmentInfoRepository.save(info);
        log.info("Updated employment info for Employee id={}", id);
        return toDetailResponse(employee);
    }
    @Transactional
    public EmployeeDetailResponse changeStatus(UUID companyId, UUID id, ChangeEmployeeStatusRequest request) {
        Employee employee = findEmployee(companyId, id);
        validateAccess(employee);

        EmploymentStatus oldStatus = employee.getEmploymentStatus();
        EmploymentStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            return toDetailResponse(employee);
        }

        LocalDate effectiveDate = request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now();
        String reason = (request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason()
                : "Thay đổi trạng thái: " + oldStatus.getDescription() + " -> " + newStatus.getDescription();

        recordHistory(employee, EmployeeChangeType.STATUS_CHANGE, oldStatus.name(), newStatus.name(), effectiveDate, reason);
        employee.setEmploymentStatus(newStatus);
        Employee updatedEmployee = employeeRepository.save(employee);

        // QUY CHUẨN BẢO MẬT: Khi nhân viên nghỉ việc hoặc sa thải, tự động khóa tài khoản User liên kết
        if (newStatus == EmploymentStatus.TERMINATED || newStatus == EmploymentStatus.RESIGNED) {
            if (employee.getUserId() != null) {
                userService.updateUserStatus(employee.getUserId(), UserStatus.LOCKED);
                log.info("Automatically locked user account id={} because employee id={} was {}", employee.getUserId(), id, newStatus);
            }
        }

        log.info("Changed status for Employee id={} from {} to {}", id, oldStatus, newStatus);
        return toDetailResponse(updatedEmployee);
    }
    @Transactional(readOnly = true)
    public List<EmployeeDependentResponse> getDependents(UUID companyId, UUID employeeId) {
        Employee employee = findEmployee(companyId, employeeId);
        validateAccess(employee);
        List<EmployeeDependent> list = dependentRepository.findByEmployeeId(employeeId);
        return employeeMapper.toDependentResponseList(list);
    }
    @Transactional
    public EmployeeDependentResponse createDependent(UUID companyId, UUID employeeId, CreateDependentRequest request) {
        Employee employee = findEmployee(companyId, employeeId);
        validateAccess(employee);

        EmployeeDependent dependent = employeeMapper.toDependentEntity(request);
        dependent.setEmployee(employee);
        dependent = dependentRepository.save(dependent);

        log.info("Created dependent id={} for employee id={}", dependent.getId(), employeeId);
        return employeeMapper.toDependentResponse(dependent);
    }
    @Transactional
    public void deleteDependent(UUID companyId, UUID employeeId, UUID dependentId) {
        findEmployee(companyId, employeeId);
        EmployeeDependent dependent = dependentRepository.findByIdAndEmployeeId(dependentId, employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.DEPENDENT_NOT_FOUND));
        dependentRepository.delete(dependent);
        log.info("Deleted dependent id={} for employee id={}", dependentId, employeeId);
    }
    @Transactional(readOnly = true)
    public List<EmployeeEmergencyContactResponse> getEmergencyContacts(UUID companyId, UUID employeeId) {
        Employee employee = findEmployee(companyId, employeeId);
        validateAccess(employee);
        List<EmployeeEmergencyContact> list = emergencyContactRepository.findByEmployeeId(employeeId);
        return employeeMapper.toEmergencyContactResponseList(list);
    }
    @Transactional
    public EmployeeEmergencyContactResponse createEmergencyContact(UUID companyId, UUID employeeId, CreateEmergencyContactRequest request) {
        Employee employee = findEmployee(companyId, employeeId);
        validateAccess(employee);

        EmployeeEmergencyContact contact = employeeMapper.toEmergencyContactEntity(request);
        contact.setEmployee(employee);
        contact = emergencyContactRepository.save(contact);

        log.info("Created emergency contact id={} for employee id={}", contact.getId(), employeeId);
        return employeeMapper.toEmergencyContactResponse(contact);
    }
    @Transactional
    public void deleteEmergencyContact(UUID companyId, UUID employeeId, UUID contactId) {
        findEmployee(companyId, employeeId);
        EmployeeEmergencyContact contact = emergencyContactRepository.findByIdAndEmployeeId(contactId, employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMERGENCY_CONTACT_NOT_FOUND));
        emergencyContactRepository.delete(contact);
        log.info("Deleted emergency contact id={} for employee id={}", contactId, employeeId);
    }
    @Transactional(readOnly = true)
    public List<EmployeeHistoryResponse> getEmployeeHistory(UUID companyId, UUID employeeId) {
        Employee employee = findEmployee(companyId, employeeId);
        validateAccess(employee);

        List<EmployeeHistory> list = historyRepository.findByEmployeeIdOrderByEffectiveDateDescCreatedAtDesc(employeeId);
        List<EmployeeHistoryResponse> responses = employeeMapper.toHistoryResponseList(list);

        for (EmployeeHistoryResponse res : responses) {
            if (res.getChangedByEmployeeId() != null) {
                employeeRepository.findById(res.getChangedByEmployeeId())
                        .ifPresent(e -> res.setChangedByFullName(e.getFullName()));
            }
        }
        return responses;
    }

    // --- Helper Methods ---

    private Employee findEmployee(UUID companyId, UUID id) {
        if (companyId != null) {
            return employeeRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        }
        return employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private String generateEmployeeCode(UUID companyId) {
        int year = Year.now().getValue();
        long nextIndex = employeeRepository.countByCompanyId(companyId) + 1;
        return String.format("EMP-%d-%04d", year, nextIndex);
    }

    private void handleUserAccountLinking(Employee employee, CreateEmployeeRequest request) {
        if (request.getUserId() != null) {
            userService.linkEmployeeToUser(request.getUserId(), employee.getId());
            employee.setUserId(request.getUserId());
            employeeRepository.save(employee);
        } else if (request.isAutoCreateUser() && request.getCompanyEmail() != null && !request.getCompanyEmail().isBlank()) {
            UUID newUserId = userService.createEmployeeUser(
                    employee.getId(),
                    request.getCompanyEmail(),
                    employee.getFullName(),
                    employee.getEmployeeCode(),
                    employee.getCompanyId()
            );
            if (newUserId != null) {
                employee.setUserId(newUserId);
                employeeRepository.save(employee);
            }
        }
    }

    private void recordHistory(Employee employee, EmployeeChangeType type, String oldVal, String newVal, LocalDate date, String reason) {
        UUID changedBy = SecurityUtils.getCurrentUserIdOptional()
                .flatMap(employeeRepository::findByUserId)
                .map(Employee::getId)
                .orElse(null);

        EmployeeHistory history = EmployeeHistory.builder()
                .employee(employee)
                .changeType(type)
                .oldValue(oldVal)
                .newValue(newVal)
                .effectiveDate(date)
                .changedByEmployeeId(changedBy)
                .reason(reason)
                .build();
        historyRepository.save(history);
    }

    private void validateOrganizationForeignKeys(UUID companyId, UUID unitId, UUID branchId, UUID positionId, UUID jobLevelId) {
        if (!orgUnitService.existsById(unitId)) {
            throw new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND);
        }
        if (!geographyService.existsBranchById(branchId)) {
            throw new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND);
        }
        if (!positionService.existsPositionById(positionId)) {
            throw new AppException(OrganizationErrorCode.POSITION_NOT_FOUND);
        }
        if (jobLevelId != null && !positionService.existsJobLevelById(jobLevelId)) {
            throw new AppException(OrganizationErrorCode.JOB_LEVEL_NOT_FOUND);
        }
    }

    private void applyDataScope(EmployeeFilter filter) {
        DataScope scope = permEvaluator.getDataScope("employee.view").orElse(DataScope.OWN);
        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();

        if (scope == DataScope.ALL || scope == DataScope.COMPANY) {
            return;
        }

        if (currentUserIdOpt.isEmpty()) {
            filter.setExactEmployeeId(UUID.randomUUID()); // Không thể xem nếu không định danh
            return;
        }

        Optional<Employee> currentEmpOpt = employeeRepository.findByUserId(currentUserIdOpt.get());
        if (currentEmpOpt.isEmpty()) {
            filter.setExactEmployeeId(UUID.randomUUID());
            return;
        }

        Employee currentEmp = currentEmpOpt.get();

        switch (scope) {
            case OWN -> filter.setExactEmployeeId(currentEmp.getId());
            case TEAM -> filter.setDirectManagerId(currentEmp.getId());
            case DEPARTMENT -> {
                if (currentEmp.getEmploymentInfo() != null && currentEmp.getEmploymentInfo().getOrganizationalUnitId() != null) {
                    Set<UUID> descendantIds = orgUnitService.getSelfAndDescendantUnitIds(
                            currentEmp.getCompanyId(),
                            currentEmp.getEmploymentInfo().getOrganizationalUnitId()
                    );
                    filter.setAllowedUnitIds(descendantIds);
                } else {
                    filter.setExactEmployeeId(currentEmp.getId());
                }
            }
            default -> {}
        }
    }

    private void validateAccess(Employee targetEmployee) {
        DataScope scope = permEvaluator.getDataScope("employee.view").orElse(DataScope.OWN);
        if (scope == DataScope.ALL || scope == DataScope.COMPANY) {
            return;
        }

        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();
        if (currentUserIdOpt.isEmpty()) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_ACCESS_DENIED);
        }

        Optional<Employee> currentEmpOpt = employeeRepository.findByUserId(currentUserIdOpt.get());
        if (currentEmpOpt.isEmpty()) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_ACCESS_DENIED);
        }

        Employee currentEmp = currentEmpOpt.get();

        if (scope == DataScope.OWN) {
            if (!targetEmployee.getId().equals(currentEmp.getId())) {
                throw new AppException(EmployeeErrorCode.EMPLOYEE_ACCESS_DENIED);
            }
        } else if (scope == DataScope.TEAM) {
            boolean isSelf = targetEmployee.getId().equals(currentEmp.getId());
            boolean isDirectSubordinate = targetEmployee.getEmploymentInfo() != null
                    && Objects.equals(targetEmployee.getEmploymentInfo().getManagerEmployeeId(), currentEmp.getId());
            if (!isSelf && !isDirectSubordinate) {
                throw new AppException(EmployeeErrorCode.EMPLOYEE_ACCESS_DENIED);
            }
        } else if (scope == DataScope.DEPARTMENT) {
            if (currentEmp.getEmploymentInfo() == null || currentEmp.getEmploymentInfo().getOrganizationalUnitId() == null) {
                throw new AppException(EmployeeErrorCode.EMPLOYEE_ACCESS_DENIED);
            }
            Set<UUID> allowedUnits = orgUnitService.getSelfAndDescendantUnitIds(
                    currentEmp.getCompanyId(),
                    currentEmp.getEmploymentInfo().getOrganizationalUnitId()
            );
            if (targetEmployee.getEmploymentInfo() == null
                    || !allowedUnits.contains(targetEmployee.getEmploymentInfo().getOrganizationalUnitId())) {
                throw new AppException(EmployeeErrorCode.EMPLOYEE_ACCESS_DENIED);
            }
        }
    }

    private EmployeeResponse toSummaryResponse(
            Employee emp,
            Map<UUID, CompanySummary> companyMap,
            Map<UUID, OrgUnitSummary> orgUnitMap,
            Map<UUID, BranchSummary> branchMap,
            Map<UUID, PositionSummary> positionMap,
            Map<UUID, EmployeeSummary> managerMap
    ) {
        CompanySummary comp = emp.getCompanyId() != null ? companyMap.get(emp.getCompanyId()) : null;
        OrgUnitSummary unit = null;
        BranchSummary branch = null;
        PositionSummary pos = null;
        EmployeeSummary mgr = null;

        EmployeeEmploymentInfo e = emp.getEmploymentInfo();
        if (e != null) {
            if (e.getOrganizationalUnitId() != null) {
                unit = orgUnitMap.get(e.getOrganizationalUnitId());
            }
            if (e.getBranchId() != null) {
                branch = branchMap.get(e.getBranchId());
            }
            if (e.getPositionId() != null) {
                pos = positionMap.get(e.getPositionId());
            }
            if (e.getManagerEmployeeId() != null) {
                mgr = managerMap.get(e.getManagerEmployeeId());
            }
        }

        EmployeePersonalInfo p = emp.getPersonalInfo();
        EmployeeResponse.EmployeeResponseBuilder b = EmployeeResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .company(comp)
                .userId(emp.getUserId())
                .fullName(emp.getFullName())
                .hireDate(emp.getHireDate())
                .employmentStatus(emp.getEmploymentStatus())
                .createdAt(emp.getCreatedAt());

        if (p != null) {
            b.gender(p.getGender())
             .dateOfBirth(p.getDateOfBirth())
             .phone(p.getPhone())
             .personalEmail(p.getPersonalEmail())
             .photoUrl(p.getPhotoUrl());
        }

        if (e != null) {
            b.organizationalUnit(unit)
             .branch(branch)
             .position(pos)
             .manager(mgr)
             .employmentType(e.getEmploymentType())
             .companyEmail(e.getCompanyEmail());
        }

        return b.build();
    }

    private EmployeeDetailResponse toDetailResponse(Employee emp) {
        EmployeePersonalInfo p = emp.getPersonalInfo();
        EmployeeEmploymentInfo e = emp.getEmploymentInfo();

        CompanySummary company = emp.getCompanyId() != null ? companyService.getCompanySummary(emp.getCompanyId()) : null;
        OrgUnitSummary orgUnit = null;
        BranchSummary branch = null;
        PositionSummary position = null;
        JobLevelSummary jobLevel = null;
        EmployeeSummary manager = null;

        if (e != null) {
            if (e.getOrganizationalUnitId() != null) {
                orgUnit = orgUnitService.getUnitSummary(e.getOrganizationalUnitId());
            }
            if (e.getBranchId() != null) {
                branch = geographyService.getBranchSummary(e.getBranchId());
            }
            if (e.getPositionId() != null) {
                position = positionService.getPositionSummary(e.getPositionId());
            }
            if (e.getJobLevelId() != null) {
                jobLevel = positionService.getJobLevelSummary(e.getJobLevelId());
            }
            if (e.getManagerEmployeeId() != null) {
                manager = getEmployeeSummary(e.getManagerEmployeeId());
            }
        }

        EmployeeDetailResponse.EmployeeDetailResponseBuilder b = EmployeeDetailResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .company(company)
                .userId(emp.getUserId())
                .fullName(emp.getFullName())
                .hireDate(emp.getHireDate())
                .employmentStatus(emp.getEmploymentStatus())
                .createdAt(emp.getCreatedAt())
                .updatedAt(emp.getUpdatedAt());

        if (p != null) {
            b.gender(p.getGender())
             .dateOfBirth(p.getDateOfBirth())
             .nationalIdNumber(p.getNationalIdNumber())
             .nationalIdIssueDate(p.getNationalIdIssueDate())
             .nationalIdIssuePlace(p.getNationalIdIssuePlace())
             .taxCode(p.getTaxCode())
             .socialInsuranceNumber(p.getSocialInsuranceNumber())
             .bankAccountNumber(p.getBankAccountNumber())
             .bankName(p.getBankName())
             .bankBranch(p.getBankBranch())
             .maritalStatus(p.getMaritalStatus())
             .nationality(p.getNationality())
             .personalEmail(p.getPersonalEmail())
             .phone(p.getPhone())
             .permanentAddress(p.getPermanentAddress())
             .currentAddress(p.getCurrentAddress())
             .photoUrl(p.getPhotoUrl());
        }

        if (e != null) {
            b.organizationalUnit(orgUnit)
             .branch(branch)
             .position(position)
             .jobLevel(jobLevel)
             .manager(manager)
             .employmentType(e.getEmploymentType())
             .companyEmail(e.getCompanyEmail())
             .workLocation(e.getWorkLocation())
             .probationEndDate(e.getProbationEndDate());
        }

        b.dependents(employeeMapper.toDependentResponseList(emp.getDependents()));
        b.emergencyContacts(employeeMapper.toEmergencyContactResponseList(emp.getEmergencyContacts()));

        return b.build();
    }
    @Transactional(readOnly = true)
    public Map<UUID, EmployeeSummary> getEmployeeSummaries(Set<UUID> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Employee> employees = employeeRepository.findAllById(employeeIds);
        return employees.stream()
                .collect(Collectors.toMap(
                        Employee::getId,
                        emp -> EmployeeSummary.builder()
                                .id(emp.getId())
                                .employeeCode(emp.getEmployeeCode())
                                .fullName(emp.getFullName())
                                .photoUrl(emp.getPersonalInfo() != null ? emp.getPersonalInfo().getPhotoUrl() : null)
                                .companyEmail(emp.getEmploymentInfo() != null ? emp.getEmploymentInfo().getCompanyEmail() : null)
                                .build(),
                        (existing, replacement) -> existing
                ));
    }
    @Transactional(readOnly = true)
    public EmployeeSummary getEmployeeSummary(UUID employeeId) {
        if (employeeId == null) return null;
        return employeeRepository.findById(employeeId)
                .map(emp -> EmployeeSummary.builder()
                        .id(emp.getId())
                        .employeeCode(emp.getEmployeeCode())
                        .fullName(emp.getFullName())
                        .photoUrl(emp.getPersonalInfo() != null ? emp.getPersonalInfo().getPhotoUrl() : null)
                        .companyEmail(emp.getEmploymentInfo() != null ? emp.getEmploymentInfo().getCompanyEmail() : null)
                        .build())
                .orElse(null);
    }
    @Transactional(readOnly = true)
    public Optional<UUID> findEmployeeIdByUserId(UUID userId) {
        if (userId == null) return Optional.empty();
        return employeeRepository.findByUserId(userId).map(Employee::getId);
    }
    @Transactional
    public void updateEmploymentStatus(UUID employeeId, EmploymentStatus status) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        emp.setEmploymentStatus(status);
        employeeRepository.save(emp);
        log.info("Updated EmploymentStatus to {} for employee id={}", status, employeeId);
    }
    @Transactional(readOnly = true)
    public EmployeeDetailResponse getEmployeeByIdInternal(UUID employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        return toDetailResponse(emp);
    }
}

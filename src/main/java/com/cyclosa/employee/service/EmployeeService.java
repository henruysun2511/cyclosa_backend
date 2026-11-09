package com.cyclosa.employee.service;

import com.cyclosa.auth.dto.request.CreateUserRequest;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.employee.dto.request.CreateEmployeeRequest;
import com.cyclosa.employee.dto.request.EmployeeFilterRequest;
import com.cyclosa.employee.dto.request.UpdateEmployeeRequest;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.dto.response.EmployeeSummaryResponse;
import com.cyclosa.employee.entity.*;
import com.cyclosa.employee.entity.enums.AssignmentChangeReason;
import com.cyclosa.employee.entity.enums.EmployeeStatus;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.mapper.EmployeeMapper;
import com.cyclosa.employee.repository.*;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.Company;
import com.cyclosa.organization.entity.Department;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.JobTitle;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "employeeCode", "lastName", "firstName", "startDate", "createdAt"
    );

    private final EmployeeRepository employeeRepository;
    private final EmployeeIdentityDocumentRepository identityDocumentRepository;
    private final EmployeeBankAccountRepository bankAccountRepository;
    private final EmployeeDependentRepository dependentRepository;
    private final EmployeeEmergencyContactRepository emergencyContactRepository;
    private final EmployeeWorkAssignmentRepository workAssignmentRepository;
    private final EmployeeStatusHistoryRepository statusHistoryRepository;
    private final ProbationEvaluationRepository probationEvaluationRepository;
    private final FaceEnrollmentRepository faceEnrollmentRepository;

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final JobTitleRepository jobTitleRepository;
    private final JobLevelRepository jobLevelRepository;

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    /**
     * Sinh mã nhân viên tự động: NV-{YYYY}-{5 chữ số tự tăng}
     */
    @Transactional
    public String generateNextEmployeeCode(UUID companyId) {
        int currentYear = LocalDate.now().getYear();
        String prefix = String.format("NV-%d-", currentYear);
        long count = employeeRepository.countByPrefix(companyId, prefix);
        return String.format("%s%05d", prefix, count + 1);
    }

    /**
     * Tiếp nhận hồ sơ nhân viên mới (Hire / Onboard)
     */
    @Transactional
    public EmployeeDetailResponse createEmployee(CreateEmployeeRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.DEPARTMENT_NOT_FOUND));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND));

        JobTitle jobTitle = jobTitleRepository.findById(request.getJobTitleId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_TITLE_NOT_FOUND));

        JobLevel jobLevel = jobLevelRepository.findById(request.getJobLevelId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_LEVEL_NOT_FOUND));

        Employee manager = null;
        if (request.getManagerEmployeeId() != null) {
            manager = employeeRepository.findById(request.getManagerEmployeeId())
                    .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        }

        // Kiểm tra chống trùng MST và BHXH
        if (request.getPersonalTaxCode() != null && !request.getPersonalTaxCode().isBlank()) {
            if (employeeRepository.existsByPersonalTaxCode(request.getPersonalTaxCode().trim())) {
                throw new AppException(EmployeeErrorCode.TAX_OR_INSURANCE_EXISTS);
            }
        }
        if (request.getSocialInsuranceNumber() != null && !request.getSocialInsuranceNumber().isBlank()) {
            if (employeeRepository.existsBySocialInsuranceNumber(request.getSocialInsuranceNumber().trim())) {
                throw new AppException(EmployeeErrorCode.TAX_OR_INSURANCE_EXISTS);
            }
        }

        // Kiểm tra trùng số giấy tờ định danh ban đầu
        if (request.getIdentityDocumentNumber() != null && !request.getIdentityDocumentNumber().isBlank()) {
            if (identityDocumentRepository.existsByDocumentTypeAndDocumentNumber(
                    request.getIdentityDocumentType(), request.getIdentityDocumentNumber().trim())) {
                throw new AppException(EmployeeErrorCode.IDENTITY_DOCUMENT_EXISTS);
            }
        }

        // Tùy chọn tạo User đăng nhập
        UUID createdUserId = null;
        if (request.isCreateSystemUser() && request.getUserEmail() != null && !request.getUserEmail().isBlank()) {
            CreateUserRequest createUserRequest = CreateUserRequest.builder()
                    .email(request.getUserEmail().trim())
                    .roleCodes(List.of("EMPLOYEE"))
                    .build();
            UserResponse userResponse = userService.createUser(createUserRequest);
            createdUserId = userResponse.getId();
        }

        String employeeCode = generateNextEmployeeCode(company.getId());

        Employee employee = Employee.builder()
                .companyId(company.getId())
                .userId(createdUserId)
                .employeeCode(employeeCode)
                .lastName(request.getLastName().trim())
                .firstName(request.getFirstName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone().trim())
                .personalEmail(request.getPersonalEmail() != null ? request.getPersonalEmail().trim() : null)
                .address(request.getAddress())
                .startDate(request.getStartDate())
                .probationEndDate(request.getProbationEndDate())
                .personalTaxCode(request.getPersonalTaxCode() != null ? request.getPersonalTaxCode().trim() : null)
                .socialInsuranceNumber(request.getSocialInsuranceNumber() != null ? request.getSocialInsuranceNumber().trim() : null)
                .portraitUrl(request.getPortraitUrl())
                .sourceApplicationId(request.getSourceApplicationId())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Lưu giấy tờ định danh ban đầu nếu có
        EmployeeIdentityDocument savedDoc = null;
        if (request.getIdentityDocumentNumber() != null && !request.getIdentityDocumentNumber().isBlank()) {
            EmployeeIdentityDocument doc = EmployeeIdentityDocument.builder()
                    .employee(savedEmployee)
                    .documentType(request.getIdentityDocumentType())
                    .documentNumber(request.getIdentityDocumentNumber().trim())
                    .issueDate(request.getIdentityIssueDate() != null ? request.getIdentityIssueDate() : LocalDate.now())
                    .issuePlace(request.getIdentityIssuePlace() != null ? request.getIdentityIssuePlace() : "Cục CSQLHC về TTXH")
                    .expiryDate(request.getIdentityExpiryDate())
                    .build();
            savedDoc = identityDocumentRepository.save(doc);
        }

        // Khởi tạo dòng phân công công tác 3 chiều đầu tiên
        EmployeeWorkAssignment initialAssignment = EmployeeWorkAssignment.builder()
                .employee(savedEmployee)
                .effectiveFrom(request.getStartDate())
                .department(department)
                .branch(branch)
                .jobTitle(jobTitle)
                .jobLevel(jobLevel)
                .manager(manager)
                .employmentType(request.getEmploymentType())
                .changeReason(AssignmentChangeReason.HIRE)
                .build();
        EmployeeWorkAssignment savedAssignment = workAssignmentRepository.save(initialAssignment);

        // Khởi tạo trạng thái ban đầu: PROBATION nếu có ngày hết hạn thử việc, ngược lại ACTIVE
        EmployeeStatus initialStatus = (request.getProbationEndDate() != null && request.getProbationEndDate().isAfter(request.getStartDate()))
                ? EmployeeStatus.PROBATION
                : EmployeeStatus.ACTIVE;

        EmployeeStatusHistory statusHistory = EmployeeStatusHistory.builder()
                .employee(savedEmployee)
                .status(initialStatus)
                .effectiveFrom(request.getStartDate())
                .note("Tiếp nhận nhân sự mới")
                .build();
        EmployeeStatusHistory savedStatus = statusHistoryRepository.save(statusHistory);

        return employeeMapper.toDetailResponse(
                savedEmployee,
                savedAssignment,
                initialStatus,
                savedDoc != null ? List.of(savedDoc) : Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                List.of(savedAssignment),
                List.of(savedStatus),
                Collections.emptyList(),
                null
        );
    }

    /**
     * Tra cứu danh sách nhân viên phân trang (phục vụ View Table & Filters)
     */
    public PageData<EmployeeSummaryResponse> getEmployees(EmployeeFilterRequest filter) {
        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = PageRequest.of(
                Math.max(0, filter.getPage()),
                Math.min(100, Math.max(1, filter.getSize())),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        LocalDate today = LocalDate.now();
        Page<Employee> page = employeeRepository.searchWithFilters(
                filter.getCompanyId(),
                filter.getDepartmentId(),
                filter.getStatus(),
                kw,
                today,
                pageable
        );

        return PageData.of(page, emp -> {
            EmployeeWorkAssignment currentAssignment = workAssignmentRepository
                    .findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(emp.getId(), today)
                    .orElse(null);

            EmployeeStatus currentStatus = statusHistoryRepository
                    .findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(emp.getId(), today)
                    .map(EmployeeStatusHistory::getStatus)
                    .orElse(EmployeeStatus.ACTIVE);

            return employeeMapper.toSummaryResponse(emp, currentAssignment, currentStatus);
        });
    }

    /**
     * Chi tiết hồ sơ nhân sự đầy đủ
     */
    public EmployeeDetailResponse getEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        LocalDate today = LocalDate.now();
        EmployeeWorkAssignment currentAssignment = workAssignmentRepository
                .findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(employee.getId(), today)
                .orElse(null);

        EmployeeStatus currentStatus = statusHistoryRepository
                .findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(employee.getId(), today)
                .map(EmployeeStatusHistory::getStatus)
                .orElse(EmployeeStatus.ACTIVE);

        List<EmployeeIdentityDocument> identities = identityDocumentRepository.findAllByEmployeeId(employee.getId());
        EmployeeBankAccount bankAccount = bankAccountRepository.findByEmployeeId(employee.getId()).orElse(null);
        List<EmployeeDependent> dependents = dependentRepository.findAllByEmployeeId(employee.getId());
        List<EmployeeEmergencyContact> emergencyContacts = emergencyContactRepository.findAllByEmployeeId(employee.getId());
        List<EmployeeWorkAssignment> workAssignments = workAssignmentRepository.findAllByEmployeeIdOrderByEffectiveFromDesc(employee.getId());
        List<EmployeeStatusHistory> statusHistory = statusHistoryRepository.findAllByEmployeeIdOrderByEffectiveFromDesc(employee.getId());
        List<ProbationEvaluation> probationEvaluations = probationEvaluationRepository.findAllByEmployeeIdOrderByEvaluationDateDesc(employee.getId());
        FaceEnrollment faceEnrollment = faceEnrollmentRepository.findByEmployeeIdAndRevokedAtIsNull(employee.getId()).orElse(null);

        return employeeMapper.toDetailResponse(
                employee,
                currentAssignment,
                currentStatus,
                identities,
                bankAccount,
                dependents,
                emergencyContacts,
                workAssignments,
                statusHistory,
                probationEvaluations,
                faceEnrollment
        );
    }

    /**
     * Cập nhật thông tin cá nhân & lý lịch của nhân viên
     */
    @Transactional
    public EmployeeDetailResponse updateEmployee(UUID employeeId, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        if (request.getPersonalTaxCode() != null && !request.getPersonalTaxCode().isBlank()) {
            if (employeeRepository.existsByPersonalTaxCodeAndIdNot(request.getPersonalTaxCode().trim(), employee.getId())) {
                throw new AppException(EmployeeErrorCode.TAX_OR_INSURANCE_EXISTS);
            }
        }
        if (request.getSocialInsuranceNumber() != null && !request.getSocialInsuranceNumber().isBlank()) {
            if (employeeRepository.existsBySocialInsuranceNumberAndIdNot(request.getSocialInsuranceNumber().trim(), employee.getId())) {
                throw new AppException(EmployeeErrorCode.TAX_OR_INSURANCE_EXISTS);
            }
        }

        employee.setLastName(request.getLastName().trim());
        employee.setFirstName(request.getFirstName().trim());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setPhone(request.getPhone().trim());
        employee.setPersonalEmail(request.getPersonalEmail() != null ? request.getPersonalEmail().trim() : null);
        employee.setAddress(request.getAddress());
        if (request.getStartDate() != null) employee.setStartDate(request.getStartDate());
        if (request.getProbationEndDate() != null) employee.setProbationEndDate(request.getProbationEndDate());
        employee.setPersonalTaxCode(request.getPersonalTaxCode() != null ? request.getPersonalTaxCode().trim() : null);
        employee.setSocialInsuranceNumber(request.getSocialInsuranceNumber() != null ? request.getSocialInsuranceNumber().trim() : null);
        if (request.getPortraitUrl() != null) employee.setPortraitUrl(request.getPortraitUrl());

        employeeRepository.save(employee);
        return getEmployeeById(employeeId);
    }

    /**
     * Tra cứu hồ sơ của chính người dùng đang đăng nhập (My Profile)
     */
    public EmployeeDetailResponse getMyProfile() {
        UUID currentUserId = getCurrentAuthenticatedUserId();
        Employee employee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        return getEmployeeById(employee.getId());
    }

    /**
     * Nhân viên tự cập nhật một số thông tin cá nhân cơ bản
     */
    @Transactional
    public EmployeeDetailResponse updateMyProfile(UpdateEmployeeRequest request) {
        UUID currentUserId = getCurrentAuthenticatedUserId();
        Employee employee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        employee.setPhone(request.getPhone().trim());
        employee.setPersonalEmail(request.getPersonalEmail() != null ? request.getPersonalEmail().trim() : null);
        employee.setAddress(request.getAddress());
        if (request.getPortraitUrl() != null) employee.setPortraitUrl(request.getPortraitUrl());

        employeeRepository.save(employee);
        return getEmployeeById(employee.getId());
    }

    private UUID getCurrentAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        String principal = auth.getName();
        return userRepository.findByEmailAndDeletedFalse(principal)
                .or(() -> userRepository.findByUsernameAndDeletedFalse(principal))
                .map(User::getId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
    }
}


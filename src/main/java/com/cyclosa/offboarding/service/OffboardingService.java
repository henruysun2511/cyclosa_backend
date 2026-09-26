package com.cyclosa.offboarding.service;

import com.cyclosa.asset.service.AssetService;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.contract.dto.response.ContractResponse;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.leave.service.LeaveBalanceService;
import com.cyclosa.offboarding.dto.request.*;
import com.cyclosa.offboarding.dto.response.*;
import com.cyclosa.offboarding.entity.*;
import com.cyclosa.offboarding.enums.*;
import com.cyclosa.offboarding.exception.OffboardingErrorCode;
import com.cyclosa.offboarding.mapper.OffboardingMapper;
import com.cyclosa.offboarding.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OffboardingService {

    private final ResignationRepository resignationRepository;
    private final TerminationRepository terminationRepository;
    private final ExitInterviewRepository exitInterviewRepository;
    private final OffboardingClearanceRepository clearanceRepository;

    private final EmployeeService employeeService;
    private final ContractService contractService;
    private final AssetService assetService;
    private final LeaveBalanceService leaveBalanceService;
    private final UserService userService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final OffboardingMapper offboardingMapper;

    private static final Set<String> ALLOWED_RESIGNATION_SORT_FIELDS = Set.of("createdAt", "submittedDate", "expectedLastWorkingDate", "status");
    private static final Set<String> ALLOWED_TERMINATION_SORT_FIELDS = Set.of("createdAt", "decisionDate", "lastWorkingDate", "status");

    // =========================================================================
    // 1. RESIGNATIONS
    // =========================================================================


    @Transactional
    public ResignationResponse applyResignation(ApplyResignationRequest request, UUID currentUserId) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(OffboardingErrorCode.CURRENT_USER_NOT_LINKED_EMPLOYEE));

        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(employeeId);
        UUID companyId = employee.getCompany() != null ? employee.getCompany().getId() : null;

        // Chặn trùng lặp đơn thôi việc / quyết định chấm dứt đang xử lý
        validateNoOffboardingInProgress(employee.getId());

        LocalDate submittedDate = LocalDate.now();
        LocalDate expectedDate = request.getExpectedLastWorkingDate();

        if (expectedDate == null || !expectedDate.isAfter(submittedDate)) {
            throw new AppException(OffboardingErrorCode.INSUFFICIENT_NOTICE_PERIOD, "Ngày dự kiến nghỉ việc phải sau ngày nộp đơn");
        }

        // Kiểm tra thời hạn báo trước theo Điều 35 BLLĐ và HĐLĐ hiện tại (45 ngày cho KTH, 30 ngày cho CT, 3 ngày cho TV)
        int requiredNoticeDays = 30;
        Optional<ContractResponse> activeContract = contractService.getActiveContractByEmployee(employee.getId());
        if (activeContract.isPresent()) {
            ContractType contractType = activeContract.get().getContractType();
            if (contractType == ContractType.INDEFINITE_TERM) {
                requiredNoticeDays = 45;
            } else if (contractType == ContractType.DEFINITE_TERM) {
                requiredNoticeDays = 30;
            } else if (contractType == ContractType.PROBATION) {
                requiredNoticeDays = 3;
            }
        }

        long noticeDays = ChronoUnit.DAYS.between(submittedDate, expectedDate);
        if (noticeDays < requiredNoticeDays) {
            throw new AppException(OffboardingErrorCode.INSUFFICIENT_NOTICE_PERIOD,
                    String.format("Thời gian báo trước không đủ theo quy định Điều 35 BLLĐ (yêu cầu tối thiểu %d ngày đối với loại hợp đồng hiện tại, thực tế %d ngày)",
                            requiredNoticeDays, noticeDays));
        }

        Resignation resignation = Resignation.builder()
                .employeeId(employee.getId())
                .companyId(companyId)
                .submittedDate(submittedDate)
                .expectedLastWorkingDate(expectedDate)
                .personalReasonCategory(request.getPersonalReasonCategory())
                .reasonDetail(request.getReasonDetail())
                .status(ResignationStatus.PENDING)
                .build();

        resignation = resignationRepository.save(resignation);

        return toResignationResponse(resignation, employee);
    }


    @Transactional
    public ResignationResponse approveResignation(UUID id, ApproveResignationRequest request, UUID currentUserId) {
        Resignation resignation = resignationRepository.findById(id)
                .orElseThrow(() -> new AppException(OffboardingErrorCode.RESIGNATION_NOT_FOUND));

        if (resignation.getStatus() != ResignationStatus.PENDING) {
            throw new AppException(OffboardingErrorCode.RESIGNATION_ALREADY_PROCESSED);
        }

        UUID approverId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        if (Boolean.TRUE.equals(request.getIsApproved())) {
            resignation.setStatus(ResignationStatus.APPROVED);
            resignation.setApprovedByEmployeeId(approverId);
            resignation.setApprovedAt(LocalDateTime.now());
            if (request.getEffectiveLastWorkingDate() != null) {
                resignation.setExpectedLastWorkingDate(request.getEffectiveLastWorkingDate());
            }

            // Chỉ khởi tạo các đầu mục bàn giao mặc định khi đơn thôi việc được phê duyệt
            ensureDefaultClearances(resignation.getEmployeeId(), resignation.getCompanyId());
        } else {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new AppException(OffboardingErrorCode.REJECTION_REASON_REQUIRED);
            }
            resignation.setStatus(ResignationStatus.REJECTED);
            resignation.setRejectionReason(request.getRejectionReason());
        }

        resignation = resignationRepository.save(resignation);
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(resignation.getEmployeeId());
        return toResignationResponse(resignation, employee);
    }

    @Transactional(readOnly = true)
    public ResignationDetailResponse getResignationById(UUID id) {
        Resignation resignation = resignationRepository.findById(id)
                .orElseThrow(() -> new AppException(OffboardingErrorCode.RESIGNATION_NOT_FOUND));

        validateAccessToEmployeeOffboarding(resignation.getEmployeeId());

        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(resignation.getEmployeeId());
        return toResignationDetailResponse(resignation, employee);
    }

    @Transactional(readOnly = true)
    public PageData<ResignationResponse> getResignations(ResignationFilter filter) {
        if (filter == null) {
            filter = new ResignationFilter();
        }

        applyResignationDataScope(filter);

        Pageable pageable = filter.toPageable("createdAt", ALLOWED_RESIGNATION_SORT_FIELDS);
        Page<Resignation> page = resignationRepository.searchResignations(
                filter.getCompanyId(), filter.getEmployeeId(), filter.getStatus(), pageable
        );

        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> employeeIds = new HashSet<>();
        for (Resignation r : page.getContent()) {
            if (r.getEmployeeId() != null) employeeIds.add(r.getEmployeeId());
            if (r.getApprovedByEmployeeId() != null) employeeIds.add(r.getApprovedByEmployeeId());
        }
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);

        List<ResignationResponse> responses = page.getContent().stream().map(r -> {
            ResignationResponse res = offboardingMapper.toResponse(r);
            EmployeeSummary emp = empMap.get(r.getEmployeeId());
            res.setEmployee(emp);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            if (r.getApprovedByEmployeeId() != null) {
                EmployeeSummary approver = empMap.get(r.getApprovedByEmployeeId());
                res.setApprovedByEmployee(approver);
                if (approver != null) {
                    res.setApprovedByEmployeeName(approver.getFullName());
                }
            }
            return res;
        }).toList();

        return PageData.of(page, responses);
    }

    // =========================================================================
    // 2. TERMINATIONS
    // =========================================================================


    @Transactional
    public TerminationResponse createTermination(CreateTerminationRequest request, UUID currentUserId) {
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(request.getEmployeeId());

        // Chặn trùng lặp đơn thôi việc / quyết định chấm dứt đang xử lý
        validateNoOffboardingInProgress(employee.getId());

        UUID deciderId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);
        UUID companyId = request.getCompanyId() != null
                ? request.getCompanyId()
                : (employee.getCompany() != null ? employee.getCompany().getId() : null);

        Termination termination = Termination.builder()
                .employeeId(employee.getId())
                .companyId(companyId)
                .decisionDate(LocalDate.now())
                .lastWorkingDate(request.getLastWorkingDate())
                .decisionReasonCategory(request.getDecisionReasonCategory())
                .decidedByEmployeeId(deciderId)
                .disciplineId(request.getDisciplineId())
                .reasonDetail(request.getReasonDetail())
                .status(TerminationStatus.APPROVED)
                .build();

        termination = terminationRepository.save(termination);

        // Khởi tạo các đầu mục bàn giao mặc định
        ensureDefaultClearances(employee.getId(), companyId);

        return toTerminationResponse(termination, employee);
    }

    @Transactional(readOnly = true)
    public TerminationDetailResponse getTerminationById(UUID id) {
        Termination termination = terminationRepository.findById(id)
                .orElseThrow(() -> new AppException(OffboardingErrorCode.TERMINATION_NOT_FOUND));

        validateAccessToEmployeeOffboarding(termination.getEmployeeId());

        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(termination.getEmployeeId());
        return toTerminationDetailResponse(termination, employee);
    }

    @Transactional(readOnly = true)
    public PageData<TerminationResponse> getTerminations(TerminationFilter filter) {
        if (filter == null) {
            filter = new TerminationFilter();
        }

        applyTerminationDataScope(filter);

        Pageable pageable = filter.toPageable("createdAt", ALLOWED_TERMINATION_SORT_FIELDS);
        Page<Termination> page = terminationRepository.searchTerminations(
                filter.getCompanyId(), filter.getEmployeeId(), filter.getStatus(), pageable
        );

        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> employeeIds = new HashSet<>();
        for (Termination t : page.getContent()) {
            if (t.getEmployeeId() != null) employeeIds.add(t.getEmployeeId());
            if (t.getDecidedByEmployeeId() != null) employeeIds.add(t.getDecidedByEmployeeId());
        }
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);

        List<TerminationResponse> responses = page.getContent().stream().map(t -> {
            TerminationResponse res = offboardingMapper.toResponse(t);
            EmployeeSummary emp = empMap.get(t.getEmployeeId());
            res.setEmployee(emp);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            if (t.getDecidedByEmployeeId() != null) {
                EmployeeSummary decider = empMap.get(t.getDecidedByEmployeeId());
                res.setDecidedByEmployee(decider);
                if (decider != null) {
                    res.setDecidedByEmployeeName(decider.getFullName());
                }
            }
            return res;
        }).toList();

        return PageData.of(page, responses);
    }

    // =========================================================================
    // 3. CLEARANCES
    // =========================================================================


    @Transactional(readOnly = true)
    public List<OffboardingClearanceResponse> getClearances(UUID employeeId) {
        validateAccessToEmployeeOffboarding(employeeId);

        List<OffboardingClearance> list = clearanceRepository.findByEmployeeId(employeeId);
        long unreturnedAssets = assetService.countUnreturnedAssets(employeeId);

        Set<UUID> employeeIds = new HashSet<>();
        for (OffboardingClearance c : list) {
            if (c.getClearedByEmployeeId() != null) employeeIds.add(c.getClearedByEmployeeId());
        }
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);

        return list.stream()
                .map(c -> {
                    OffboardingClearanceResponse res = offboardingMapper.toResponse(c);
                    res.setEmployee(employeeService.getEmployeeSummary(c.getEmployeeId()));
                    if (c.getClearanceType() == ClearanceType.ASSET) {
                        res.setUnreturnedAssetCount((int) unreturnedAssets);
                    }
                    if (c.getClearedByEmployeeId() != null) {
                        EmployeeSummary clearedBy = empMap.get(c.getClearedByEmployeeId());
                        res.setClearedByEmployee(clearedBy);
                        if (clearedBy != null) {
                            res.setClearedByEmployeeName(clearedBy.getFullName());
                        }
                    }
                    return res;
                })
                .toList();
    }

    @Transactional
    public OffboardingClearanceResponse updateClearance(UpdateClearanceRequest request, UUID currentUserId) {
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(request.getEmployeeId());

        OffboardingClearance clearance = clearanceRepository
                .findByEmployeeIdAndClearanceType(employee.getId(), request.getClearanceType())
                .orElseGet(() -> OffboardingClearance.builder()
                        .employeeId(employee.getId())
                        .companyId(employee.getCompany() != null ? employee.getCompany().getId() : null)
                        .clearanceType(request.getClearanceType())
                        .build());

        // Kiểm tra ràng buộc tài sản nếu là mục ASSET
        if (request.getClearanceType() == ClearanceType.ASSET && request.getStatus() == ClearanceStatus.CLEARED) {
            long unreturned = assetService.countUnreturnedAssets(employee.getId());
            if (unreturned > 0) {
                throw new AppException(OffboardingErrorCode.UNRETURNED_ASSETS_EXIST);
            }
        }

        UUID clearedById = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        clearance.setStatus(request.getStatus());
        clearance.setNote(request.getNote());
        clearance.setClearedByEmployeeId(clearedById);
        clearance.setClearedAt(request.getStatus() == ClearanceStatus.CLEARED ? LocalDateTime.now() : null);

        // Lưu số tài sản chưa trả vào database
        if (clearance.getClearanceType() == ClearanceType.ASSET) {
            clearance.setUnreturnedAssetCount((int) assetService.countUnreturnedAssets(employee.getId()));
        }

        clearance = clearanceRepository.save(clearance);
        OffboardingClearanceResponse res = offboardingMapper.toResponse(clearance);
        res.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
        if (clearedById != null) {
            EmployeeSummary clearedBy = employeeService.getEmployeeSummary(clearedById);
            res.setClearedByEmployee(clearedBy);
            if (clearedBy != null) {
                res.setClearedByEmployeeName(clearedBy.getFullName());
            }
        }
        if (clearance.getClearanceType() == ClearanceType.ASSET) {
            res.setUnreturnedAssetCount((int) assetService.countUnreturnedAssets(employee.getId()));
        }
        return res;
    }

    // =========================================================================
    // 4. EXIT INTERVIEWS
    // =========================================================================

    @Transactional
    public ExitInterviewResponse submitExitInterview(SubmitExitInterviewRequest request, UUID currentUserId) {
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(request.getEmployeeId());
        UUID interviewerId = request.getInterviewerEmployeeId() != null
                ? request.getInterviewerEmployeeId()
                : employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        LocalDate date = request.getInterviewDate() != null ? request.getInterviewDate() : LocalDate.now();

        ExitInterview exitInterview = exitInterviewRepository.findByEmployeeId(employee.getId())
                .orElseGet(() -> ExitInterview.builder()
                        .employeeId(employee.getId())
                        .companyId(employee.getCompany() != null ? employee.getCompany().getId() : null)
                        .build());

        exitInterview.setInterviewDate(date);
        exitInterview.setInterviewerEmployeeId(interviewerId);
        exitInterview.setFeedbackSummary(request.getFeedbackSummary());
        exitInterview.setWouldRecommendCompany(request.getWouldRecommendCompany());
        exitInterview.setReasonForLeaving(request.getReasonForLeaving());
        exitInterview.setSuggestions(request.getSuggestions());

        exitInterview = exitInterviewRepository.save(exitInterview);
        return toExitInterviewResponse(exitInterview, employee);
    }

    @Transactional(readOnly = true)
    public ExitInterviewDetailResponse getExitInterview(UUID employeeId) {
        validateAccessToEmployeeOffboarding(employeeId);

        ExitInterview interview = exitInterviewRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new AppException(OffboardingErrorCode.EXIT_INTERVIEW_NOT_FOUND));
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(employeeId);
        return toExitInterviewDetailResponse(interview, employee);
    }

    // =========================================================================
    // 5. SUMMARY & COMPLETE OFFBOARDING
    // =========================================================================


    @Transactional(readOnly = true)
    public OffboardingSummaryResponse getOffboardingSummary(UUID employeeId) {
        validateAccessToEmployeeOffboarding(employeeId);

        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(employeeId);

        String deptName = emp.getOrganizationalUnit() != null ? emp.getOrganizationalUnit().getName() : null;
        String posName = emp.getPosition() != null ? emp.getPosition().getName() : null;

        // Xác định loại thôi việc
        LocalDate lastDate = null;
        String offType = "UNKNOWN";
        String reason = null;

        List<Resignation> resignations = resignationRepository.findByEmployeeIdOrderByCreatedAtDesc(emp.getId());
        if (!resignations.isEmpty()) {
            Resignation r = resignations.get(0);
            offType = "RESIGNATION (" + r.getStatus() + ")";
            lastDate = r.getExpectedLastWorkingDate();
            reason = r.getPersonalReasonCategory() + (r.getReasonDetail() != null ? ": " + r.getReasonDetail() : "");
        }

        List<Termination> terminations = terminationRepository.findByEmployeeIdOrderByCreatedAtDesc(emp.getId());
        if (!terminations.isEmpty() && (resignations.isEmpty() || terminations.get(0).getCreatedAt().isAfter(resignations.get(0).getCreatedAt()))) {
            Termination t = terminations.get(0);
            offType = "TERMINATION (" + t.getStatus() + ")";
            lastDate = t.getLastWorkingDate();
            reason = t.getDecisionReasonCategory() + (t.getReasonDetail() != null ? ": " + t.getReasonDetail() : "");
        }

        // Đếm tài sản chưa trả
        long unreturnedAssets = assetService.countUnreturnedAssets(emp.getId());

        // Lấy ngày phép tồn
        int currentYear = LocalDate.now().getYear();
        BigDecimal remainingLeave = leaveBalanceService.getTotalRemainingLeaveDays(emp.getId(), currentYear);

        // Danh sách bàn giao
        List<OffboardingClearanceResponse> clearances = getClearances(emp.getId());
        boolean isFullyCleared = !clearances.isEmpty()
                && clearances.stream().allMatch(c -> c.getStatus() == ClearanceStatus.CLEARED)
                && unreturnedAssets == 0;

        ExitInterviewResponse exitInterview = exitInterviewRepository.findByEmployeeId(emp.getId())
                .map(i -> toExitInterviewResponse(i, emp))
                .orElse(null);

        return OffboardingSummaryResponse.builder()
                .employeeId(emp.getId())
                .employeeName(emp.getFullName())
                .employeeCode(emp.getEmployeeCode())
                .departmentName(deptName)
                .positionName(posName)
                .employmentStatus(emp.getEmploymentStatus())
                .lastWorkingDate(lastDate)
                .offboardingType(offType)
                .reason(reason)
                .isFullyCleared(isFullyCleared)
                .unreturnedAssetCount(unreturnedAssets)
                .remainingAnnualLeaveDays(remainingLeave)
                .clearances(clearances)
                .exitInterview(exitInterview)
                .build();
    }


    @Transactional
    public OffboardingSummaryResponse completeOffboarding(UUID employeeId, UUID currentUserId) {
        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(employeeId);

        // 1. Kiểm tra tồn tại ít nhất 1 bản ghi Resignation hoặc Termination ở trạng thái APPROVED
        Optional<Resignation> approvedResignation = resignationRepository.findFirstByEmployeeIdAndStatus(emp.getId(), ResignationStatus.APPROVED);
        Optional<Termination> approvedTermination = terminationRepository.findFirstByEmployeeIdAndStatus(emp.getId(), TerminationStatus.APPROVED);

        if (approvedResignation.isEmpty() && approvedTermination.isEmpty()) {
            throw new AppException(OffboardingErrorCode.NO_APPROVED_OFFBOARDING);
        }

        // 2. Kiểm tra tài sản chưa hoàn trả
        long unreturned = assetService.countUnreturnedAssets(emp.getId());
        if (unreturned > 0) {
            throw new AppException(OffboardingErrorCode.UNRETURNED_ASSETS_EXIST);
        }

        // 3. Kiểm tra tất cả các thủ tục bàn giao (bắt buộc danh sách clearances không rỗng và 100% CLEARED)
        List<OffboardingClearance> clearances = clearanceRepository.findByEmployeeId(emp.getId());
        if (clearances.isEmpty() || clearances.stream().anyMatch(c -> c.getStatus() != ClearanceStatus.CLEARED)) {
            throw new AppException(OffboardingErrorCode.CLEARANCES_INCOMPLETE);
        }

        // 4. Cập nhật trạng thái nhân viên: Nếu có Termination thì TERMINATED, ngược lại RESIGNED
        EmploymentStatus targetStatus = approvedTermination.isPresent()
                ? EmploymentStatus.TERMINATED
                : EmploymentStatus.RESIGNED;
        employeeService.updateEmploymentStatus(emp.getId(), targetStatus);

        // 5. Khóa tài khoản User tương ứng nếu có qua UserService
        if (emp.getUserId() != null) {
            userService.updateUserStatus(emp.getUserId(), UserStatus.DISABLED);
        }

        // 6. Đánh dấu Resignation / Termination là COMPLETED
        approvedResignation.ifPresent(r -> {
            r.setStatus(ResignationStatus.COMPLETED);
            resignationRepository.save(r);
        });

        approvedTermination.ifPresent(t -> {
            t.setStatus(TerminationStatus.COMPLETED);
            terminationRepository.save(t);
        });

        return getOffboardingSummary(emp.getId());
    }

    // =========================================================================
    // Helper Methods & DataScope
    // =========================================================================

    private void validateNoOffboardingInProgress(UUID employeeId) {
        boolean hasPendingOrApprovedResignation = resignationRepository.existsByEmployeeIdAndStatusIn(
                employeeId, List.of(ResignationStatus.PENDING, ResignationStatus.APPROVED)
        );
        boolean hasActiveTermination = terminationRepository.existsByEmployeeIdAndStatusIn(
                employeeId, List.of(TerminationStatus.DRAFT, TerminationStatus.APPROVED)
        );
        if (hasPendingOrApprovedResignation || hasActiveTermination) {
            throw new AppException(OffboardingErrorCode.OFFBOARDING_ALREADY_IN_PROGRESS);
        }
    }

    private void validateAccessToEmployeeOffboarding(UUID targetEmployeeId) {
        if (permEvaluator.has("offboarding.view") || permEvaluator.has("offboarding.manage")) {
            return;
        }
        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();
        if (currentUserIdOpt.isPresent()) {
            UUID myEmpId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get()).orElse(null);
            if (myEmpId != null && myEmpId.equals(targetEmployeeId)) {
                return;
            }
        }
        throw new AppException(OffboardingErrorCode.UNAUTHORIZED_OFFBOARDING_ACCESS);
    }

    private void ensureDefaultClearances(UUID employeeId, UUID companyId) {
        List<ClearanceType> defaultTypes = List.of(
                ClearanceType.ASSET,
                ClearanceType.IT,
                ClearanceType.FINANCE,
                ClearanceType.HR,
                ClearanceType.DEPARTMENT
        );

        for (ClearanceType type : defaultTypes) {
            if (clearanceRepository.findByEmployeeIdAndClearanceType(employeeId, type).isEmpty()) {
                clearanceRepository.save(OffboardingClearance.builder()
                        .employeeId(employeeId)
                        .companyId(companyId)
                        .clearanceType(type)
                        .status(ClearanceStatus.PENDING)
                        .build());
            }
        }
    }

    private void applyResignationDataScope(ResignationFilter filter) {
        DataScope scope = permEvaluator.getDataScope("offboarding.view").orElse(DataScope.OWN);
        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();

        switch (scope) {
            case OWN -> {
                if (currentUserIdOpt.isPresent()) {
                    UUID myEmpId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get()).orElse(null);
                    filter.setEmployeeId(myEmpId);
                }
            }
            case DEPARTMENT, TEAM, COMPANY -> {
                if (currentUserIdOpt.isPresent() && filter.getCompanyId() == null) {
                    UUID myEmpId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get()).orElse(null);
                    if (myEmpId != null) {
                        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(myEmpId);
                        if (emp.getCompany() != null) {
                            filter.setCompanyId(emp.getCompany().getId());
                        }
                    }
                }
            }
            case ALL -> {
            }
        }
    }

    private void applyTerminationDataScope(TerminationFilter filter) {
        DataScope scope = permEvaluator.getDataScope("offboarding.view").orElse(DataScope.OWN);
        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();

        switch (scope) {
            case OWN -> {
                if (currentUserIdOpt.isPresent()) {
                    UUID myEmpId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get()).orElse(null);
                    filter.setEmployeeId(myEmpId);
                }
            }
            case DEPARTMENT, TEAM, COMPANY -> {
                if (currentUserIdOpt.isPresent() && filter.getCompanyId() == null) {
                    UUID myEmpId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get()).orElse(null);
                    if (myEmpId != null) {
                        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(myEmpId);
                        if (emp.getCompany() != null) {
                            filter.setCompanyId(emp.getCompany().getId());
                        }
                    }
                }
            }
            case ALL -> {
            }
        }
    }

    private ResignationResponse toResignationResponse(Resignation resignation, EmployeeDetailResponse employee) {
        ResignationResponse res = offboardingMapper.toResponse(resignation);
        if (employee != null) {
            res.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
            res.setEmployeeName(employee.getFullName());
            res.setEmployeeCode(employee.getEmployeeCode());
            if (employee.getOrganizationalUnit() != null) {
                res.setDepartment(employee.getOrganizationalUnit());
                res.setDepartmentName(employee.getOrganizationalUnit().getName());
            }
            if (employee.getCompany() != null) {
                res.setCompany(employee.getCompany());
            }
        }
        if (resignation.getApprovedByEmployeeId() != null) {
            EmployeeSummary approver = employeeService.getEmployeeSummary(resignation.getApprovedByEmployeeId());
            res.setApprovedByEmployee(approver);
            if (approver != null) {
                res.setApprovedByEmployeeName(approver.getFullName());
            }
        }
        return res;
    }

    private ResignationDetailResponse toResignationDetailResponse(Resignation resignation, EmployeeDetailResponse employee) {
        ResignationDetailResponse res = offboardingMapper.toDetailResponse(resignation);
        if (employee != null) {
            res.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
            res.setEmployeeName(employee.getFullName());
            res.setEmployeeCode(employee.getEmployeeCode());
            if (employee.getOrganizationalUnit() != null) {
                res.setDepartment(employee.getOrganizationalUnit());
                res.setDepartmentName(employee.getOrganizationalUnit().getName());
            }
            if (employee.getCompany() != null) {
                res.setCompany(employee.getCompany());
            }
        }
        if (resignation.getApprovedByEmployeeId() != null) {
            EmployeeSummary approver = employeeService.getEmployeeSummary(resignation.getApprovedByEmployeeId());
            res.setApprovedByEmployee(approver);
            if (approver != null) {
                res.setApprovedByEmployeeName(approver.getFullName());
            }
        }
        return res;
    }

    private TerminationResponse toTerminationResponse(Termination termination, EmployeeDetailResponse employee) {
        TerminationResponse res = offboardingMapper.toResponse(termination);
        if (employee != null) {
            res.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
            res.setEmployeeName(employee.getFullName());
            res.setEmployeeCode(employee.getEmployeeCode());
            if (employee.getOrganizationalUnit() != null) {
                res.setDepartment(employee.getOrganizationalUnit());
                res.setDepartmentName(employee.getOrganizationalUnit().getName());
            }
            if (employee.getCompany() != null) {
                res.setCompany(employee.getCompany());
            }
        }
        if (termination.getDecidedByEmployeeId() != null) {
            EmployeeSummary decider = employeeService.getEmployeeSummary(termination.getDecidedByEmployeeId());
            res.setDecidedByEmployee(decider);
            if (decider != null) {
                res.setDecidedByEmployeeName(decider.getFullName());
            }
        }
        return res;
    }

    private TerminationDetailResponse toTerminationDetailResponse(Termination termination, EmployeeDetailResponse employee) {
        TerminationDetailResponse res = offboardingMapper.toDetailResponse(termination);
        if (employee != null) {
            res.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
            res.setEmployeeName(employee.getFullName());
            res.setEmployeeCode(employee.getEmployeeCode());
            if (employee.getOrganizationalUnit() != null) {
                res.setDepartment(employee.getOrganizationalUnit());
                res.setDepartmentName(employee.getOrganizationalUnit().getName());
            }
            if (employee.getCompany() != null) {
                res.setCompany(employee.getCompany());
            }
        }
        if (termination.getDecidedByEmployeeId() != null) {
            EmployeeSummary decider = employeeService.getEmployeeSummary(termination.getDecidedByEmployeeId());
            res.setDecidedByEmployee(decider);
            if (decider != null) {
                res.setDecidedByEmployeeName(decider.getFullName());
            }
        }
        return res;
    }

    private ExitInterviewResponse toExitInterviewResponse(ExitInterview interview, EmployeeDetailResponse employee) {
        ExitInterviewResponse res = offboardingMapper.toResponse(interview);
        if (employee != null) {
            res.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
            res.setEmployeeName(employee.getFullName());
            res.setEmployeeCode(employee.getEmployeeCode());
            if (employee.getCompany() != null) {
                res.setCompany(employee.getCompany());
            }
        }
        if (interview.getInterviewerEmployeeId() != null) {
            EmployeeSummary interviewer = employeeService.getEmployeeSummary(interview.getInterviewerEmployeeId());
            res.setInterviewerEmployee(interviewer);
            if (interviewer != null) {
                res.setInterviewerEmployeeName(interviewer.getFullName());
            }
        }
        return res;
    }

    private ExitInterviewDetailResponse toExitInterviewDetailResponse(ExitInterview interview, EmployeeDetailResponse employee) {
        ExitInterviewDetailResponse res = offboardingMapper.toDetailResponse(interview);
        if (employee != null) {
            res.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
            res.setEmployeeName(employee.getFullName());
            res.setEmployeeCode(employee.getEmployeeCode());
            if (employee.getCompany() != null) {
                res.setCompany(employee.getCompany());
            }
        }
        if (interview.getInterviewerEmployeeId() != null) {
            EmployeeSummary interviewer = employeeService.getEmployeeSummary(interview.getInterviewerEmployeeId());
            res.setInterviewerEmployee(interviewer);
            if (interviewer != null) {
                res.setInterviewerEmployeeName(interviewer.getFullName());
            }
        }
        return res;
    }
}

package com.cyclosa.discipline.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.discipline.dto.request.*;
import com.cyclosa.discipline.dto.response.*;
import com.cyclosa.discipline.entity.Discipline;
import com.cyclosa.discipline.entity.Grievance;
import com.cyclosa.discipline.entity.Reward;
import com.cyclosa.discipline.enums.*;
import com.cyclosa.discipline.exception.DisciplineErrorCode;
import com.cyclosa.discipline.mapper.DisciplineMapper;
import com.cyclosa.discipline.repository.DisciplineRepository;
import com.cyclosa.discipline.repository.GrievanceRepository;
import com.cyclosa.discipline.repository.RewardRepository;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.enums.FamilyRelationship;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.leave.service.LeaveRequestService;
import com.cyclosa.offboarding.dto.request.CreateTerminationRequest;
import com.cyclosa.offboarding.enums.TerminationReason;
import com.cyclosa.offboarding.service.OffboardingService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardDisciplineService {

    private final RewardRepository rewardRepository;
    private final DisciplineRepository disciplineRepository;
    private final GrievanceRepository grievanceRepository;

    private final EmployeeService employeeService;
    private final LeaveRequestService leaveRequestService;
    private final OffboardingService offboardingService;
    private final OrganizationalUnitService orgUnitService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final DisciplineMapper disciplineMapper;

    private static final Set<String> ALLOWED_REWARD_SORT_FIELDS = Set.of("decidedDate", "amount", "createdAt", "title");
    private static final Set<String> ALLOWED_DISCIPLINE_SORT_FIELDS = Set.of("createdAt", "decisionDate", "violationDate", "status", "disciplineType");
    private static final Set<String> ALLOWED_GRIEVANCE_SORT_FIELDS = Set.of("createdAt", "submittedDate", "status", "category");

    // =========================================================================
    // 1. REWARDS
    // =========================================================================

    @Transactional
    public RewardResponse createReward(CreateRewardRequest request) {
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(request.getEmployeeId());
        UUID companyId = request.getCompanyId() != null
                ? request.getCompanyId()
                : (employee.getCompany() != null ? employee.getCompany().getId() : null);

        Reward reward = Reward.builder()
                .employeeId(employee.getId())
                .companyId(companyId)
                .rewardType(request.getRewardType())
                .title(request.getTitle())
                .amount(request.getAmount())
                .reason(request.getReason())
                .decidedByEmployeeId(request.getDecidedByEmployeeId())
                .decidedDate(request.getDecidedDate())
                .pushedToPayroll(false)
                .build();

        reward = rewardRepository.save(reward);
        return toRewardResponse(reward, employee);
    }

    @Transactional(readOnly = true)
    public RewardDetailResponse getRewardById(UUID id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.REWARD_NOT_FOUND));
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(reward.getEmployeeId());
        return toRewardDetailResponse(reward, employee);
    }


    @Transactional(readOnly = true)
    public PageData<RewardResponse> getRewards(RewardFilter filter) {
        if (filter == null) {
            filter = new RewardFilter();
        }

        applyRewardDataScope(filter);

        String kw = PageableUtils.normalizeKeyword(filter.getSearch() != null ? filter.getSearch() : filter.getKeyword());
        Pageable pageable = filter.toPageable("decidedDate", ALLOWED_REWARD_SORT_FIELDS);

        Page<Reward> page = rewardRepository.searchRewards(filter.getCompanyId(), filter.getEmployeeId(), kw, pageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> employeeIds = new HashSet<>();
        for (Reward r : page.getContent()) {
            if (r.getEmployeeId() != null) employeeIds.add(r.getEmployeeId());
            if (r.getDecidedByEmployeeId() != null) employeeIds.add(r.getDecidedByEmployeeId());
        }
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);

        List<RewardResponse> responses = page.getContent().stream().map(r -> {
            RewardResponse res = disciplineMapper.toResponse(r);
            EmployeeSummary emp = empMap.get(r.getEmployeeId());
            res.setEmployee(emp);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            if (r.getDecidedByEmployeeId() != null) {
                EmployeeSummary decider = empMap.get(r.getDecidedByEmployeeId());
                res.setDecidedBy(decider);
                if (decider != null) {
                    res.setDecidedByEmployeeName(decider.getFullName());
                }
            }
            return res;
        }).toList();

        return PageData.of(page, responses);
    }

    @Transactional
    public RewardResponse updateReward(UUID id, UpdateRewardRequest request) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.REWARD_NOT_FOUND));

        if (Boolean.TRUE.equals(reward.getPushedToPayroll())) {
            throw new AppException(DisciplineErrorCode.REWARD_ALREADY_PUSHED_TO_PAYROLL);
        }

        if (request.getRewardType() != null) {
            reward.setRewardType(request.getRewardType());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            reward.setTitle(request.getTitle());
        }
        if (request.getAmount() != null) {
            reward.setAmount(request.getAmount());
        }
        if (request.getReason() != null) {
            reward.setReason(request.getReason());
        }
        if (request.getDecidedByEmployeeId() != null) {
            reward.setDecidedByEmployeeId(request.getDecidedByEmployeeId());
        }
        if (request.getDecidedDate() != null) {
            reward.setDecidedDate(request.getDecidedDate());
        }

        reward = rewardRepository.save(reward);
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(reward.getEmployeeId());
        return toRewardResponse(reward, employee);
    }


    @Transactional
    public void deleteReward(UUID id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.REWARD_NOT_FOUND));
        if (Boolean.TRUE.equals(reward.getPushedToPayroll())) {
            throw new AppException(DisciplineErrorCode.REWARD_ALREADY_PUSHED_TO_PAYROLL);
        }
        rewardRepository.delete(reward);
    }

    // =========================================================================
    // 2. DISCIPLINES
    // =========================================================================


    @Transactional
    public DisciplineResponse createDiscipline(CreateDisciplineRequest request) {
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(request.getEmployeeId());
        UUID companyId = request.getCompanyId() != null
                ? request.getCompanyId()
                : (employee.getCompany() != null ? employee.getCompany().getId() : null);

        LocalDate today = LocalDate.now();

        // 1. Kiểm tra ngày vi phạm, ngày quyết định và ngày họp
        if (request.getViolationDate().isAfter(today)) {
            throw AppException.badRequest("Ngày vi phạm không thể ở tương lai");
        }
        if (request.getDecisionDate() != null && request.getDecisionDate().isBefore(request.getViolationDate())) {
            throw AppException.badRequest("Ngày quyết định kỷ luật không được trước ngày vi phạm");
        }
        if (request.getMeetingDate() != null && request.getMeetingDate().isBefore(request.getViolationDate())) {
            throw AppException.badRequest("Ngày họp xử lý kỷ luật không được trước ngày vi phạm");
        }

        // 2. Kiểm tra trạng thái nhân sự (chỉ xử lý kỷ luật nhân viên đang làm việc)
        if (employee.getEmploymentStatus() == EmploymentStatus.RESIGNED || employee.getEmploymentStatus() == EmploymentStatus.TERMINATED) {
            throw new AppException(DisciplineErrorCode.EMPLOYEE_NOT_ACTIVE, "Không thể lập hồ sơ kỷ luật cho nhân viên đã thôi việc hoặc đã bị chấm dứt hợp đồng");
        }

        // 3. Điều 122.4 BLLĐ 2019: Cấm xử lý kỷ luật lao động trong các trường hợp bảo vệ luật định
        // a. Đang nghỉ ốm đau, điều dưỡng, nghỉ việc được NSDLĐ đồng ý (nghỉ phép đã duyệt)
        boolean isOnLeave = employee.getEmploymentStatus() == EmploymentStatus.ON_LEAVE
                || leaveRequestService.isEmployeeOnApprovedLeave(employee.getId(), today)
                || (request.getMeetingDate() != null && leaveRequestService.isEmployeeOnApprovedLeave(employee.getId(), request.getMeetingDate()));
        if (isOnLeave) {
            throw new AppException(DisciplineErrorCode.DISCIPLINE_RESTRICTED_BY_LAW,
                    "Không được xử lý kỷ luật lao động đối với người lao động đang trong thời gian nghỉ ốm đau, điều dưỡng hoặc nghỉ phép được duyệt theo Điều 122.4.a BLLĐ");
        }

        // b. Lao động nữ mang thai, nghỉ thai sản, hoặc người lao động nuôi con dưới 12 tháng tuổi
        boolean hasChildUnder12Months = employee.getDependents() != null && employee.getDependents().stream()
                .anyMatch(d -> d.getRelationship() == FamilyRelationship.CHILD
                        && d.getDateOfBirth() != null
                        && d.getDateOfBirth().isAfter(today.minusMonths(12)));
        if (hasChildUnder12Months) {
            throw new AppException(DisciplineErrorCode.DISCIPLINE_RESTRICTED_BY_LAW,
                    "Không được xử lý kỷ luật lao động đối với người lao động đang nuôi con dưới 12 tháng tuổi theo Điều 122.4.d BLLĐ");
        }

        // 4. Tính thời hiệu xử lý kỷ luật theo Điều 123 BLLĐ
        LocalDate deadline;
        if (request.getViolationCategory() == ViolationCategory.FINANCIAL_ASSET_CONFIDENTIAL) {
            deadline = request.getViolationDate().plusMonths(12);
        } else {
            deadline = request.getViolationDate().plusMonths(6);
        }

        if (today.isAfter(deadline)) {
            throw new AppException(DisciplineErrorCode.STATUTE_OF_LIMITATIONS_EXPIRED);
        }

        // 5. Validate căn cứ sa thải (Điều 125 BLLĐ) và Workflow duyệt sa thải (Rule 367)
        if (request.getDisciplineType() == DisciplineType.SA_THAI) {
            if (request.getDismissalGround() == null) {
                throw new AppException(DisciplineErrorCode.DISMISSAL_REQUIRES_GROUND);
            }
            if (request.getStatus() == DisciplineStatus.DECIDED) {
                throw new AppException(DisciplineErrorCode.TERMINATION_REQUIRES_APPROVAL);
            }
        }

        // 6. Validate thời gian kéo dài nâng lương (Điều 124.2 BLLĐ)
        if (request.getDisciplineType() == DisciplineType.KEO_DAI_NANG_LUONG) {
            if (request.getSalaryExtensionMonths() == null || request.getSalaryExtensionMonths() <= 0 || request.getSalaryExtensionMonths() > 6) {
                throw new AppException(DisciplineErrorCode.SALARY_EXTENSION_MONTHS_INVALID);
            }
        }

        // 7. Tính ngày xóa kỷ luật (Expiry date) theo Điều 126 BLLĐ
        LocalDate expiryDate = null;
        if (request.getDecisionDate() != null) {
            if (request.getDisciplineType() == DisciplineType.KHIEN_TRACH) {
                expiryDate = request.getDecisionDate().plusMonths(3);
            } else if (request.getDisciplineType() == DisciplineType.KEO_DAI_NANG_LUONG || request.getDisciplineType() == DisciplineType.CACH_CHUC) {
                expiryDate = request.getDecisionDate().plusMonths(6);
            }
        }

        DisciplineStatus status = request.getStatus() != null ? request.getStatus() : DisciplineStatus.DRAFT;

        Discipline discipline = Discipline.builder()
                .employeeId(employee.getId())
                .companyId(companyId)
                .violationDate(request.getViolationDate())
                .violationCategory(request.getViolationCategory())
                .statuteOfLimitationDeadline(deadline)
                .evidenceFiles(request.getEvidenceFiles())
                .handbookReference(request.getHandbookReference())
                .meetingDate(request.getMeetingDate())
                .meetingAttendees(request.getMeetingAttendees())
                .disciplineType(request.getDisciplineType())
                .dismissalGround(request.getDismissalGround())
                .salaryExtensionMonths(request.getSalaryExtensionMonths())
                .reason(request.getReason())
                .decisionDate(request.getDecisionDate())
                .decidedByEmployeeId(request.getDecidedByEmployeeId())
                .status(status)
                .expiryDate(expiryDate)
                .fileUrl(request.getFileUrl())
                .build();

        discipline = disciplineRepository.save(discipline);
        return toDisciplineResponse(discipline, employee);
    }


    @Transactional
    public DisciplineResponse updateDiscipline(UUID id, UpdateDisciplineRequest request) {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.DISCIPLINE_NOT_FOUND));

        if (discipline.getStatus() == DisciplineStatus.DECIDED || discipline.getStatus() == DisciplineStatus.EXPIRED) {
            throw new AppException(DisciplineErrorCode.DISCIPLINE_ALREADY_APPROVED);
        }

        if (request.getDisciplineType() != null) {
            discipline.setDisciplineType(request.getDisciplineType());
        }
        if (request.getDismissalGround() != null) {
            discipline.setDismissalGround(request.getDismissalGround());
        }
        if (discipline.getDisciplineType() == DisciplineType.SA_THAI && discipline.getDismissalGround() == null) {
            throw new AppException(DisciplineErrorCode.DISMISSAL_REQUIRES_GROUND);
        }

        if (request.getSalaryExtensionMonths() != null) {
            if (discipline.getDisciplineType() == DisciplineType.KEO_DAI_NANG_LUONG &&
                    (request.getSalaryExtensionMonths() <= 0 || request.getSalaryExtensionMonths() > 6)) {
                throw new AppException(DisciplineErrorCode.SALARY_EXTENSION_MONTHS_INVALID);
            }
            discipline.setSalaryExtensionMonths(request.getSalaryExtensionMonths());
        }
        if (request.getReason() != null) {
            discipline.setReason(request.getReason());
        }
        if (request.getMeetingDate() != null) {
            discipline.setMeetingDate(request.getMeetingDate());
        }
        if (request.getMeetingAttendees() != null) {
            discipline.setMeetingAttendees(request.getMeetingAttendees());
        }
        if (request.getEvidenceFiles() != null) {
            discipline.setEvidenceFiles(request.getEvidenceFiles());
        }
        if (request.getFileUrl() != null) {
            discipline.setFileUrl(request.getFileUrl());
        }
        if (request.getDecisionDate() != null) {
            discipline.setDecisionDate(request.getDecisionDate());
            if (discipline.getDisciplineType() == DisciplineType.KHIEN_TRACH) {
                discipline.setExpiryDate(request.getDecisionDate().plusMonths(3));
            } else if (discipline.getDisciplineType() == DisciplineType.KEO_DAI_NANG_LUONG || discipline.getDisciplineType() == DisciplineType.CACH_CHUC) {
                discipline.setExpiryDate(request.getDecisionDate().plusMonths(6));
            }
        }
        if (request.getDecidedByEmployeeId() != null) {
            discipline.setDecidedByEmployeeId(request.getDecidedByEmployeeId());
        }
        if (request.getStatus() != null) {
            discipline.setStatus(request.getStatus());
        }

        discipline = disciplineRepository.save(discipline);
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(discipline.getEmployeeId());
        return toDisciplineResponse(discipline, employee);
    }

    @Transactional(readOnly = true)
    public DisciplineDetailResponse getDisciplineById(UUID id) {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.DISCIPLINE_NOT_FOUND));
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(discipline.getEmployeeId());
        return toDisciplineDetailResponse(discipline, employee);
    }

    @Transactional(readOnly = true)
    public PageData<DisciplineResponse> getDisciplines(DisciplineFilter filter) {
        if (filter == null) {
            filter = new DisciplineFilter();
        }

        applyDisciplineDataScope(filter);

        Pageable pageable = filter.toPageable("createdAt", ALLOWED_DISCIPLINE_SORT_FIELDS);
        Page<Discipline> page = disciplineRepository.searchDisciplines(
                filter.getCompanyId(), filter.getEmployeeId(), filter.getStatus(), filter.getDisciplineType(), pageable
        );

        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> employeeIds = new HashSet<>();
        for (Discipline d : page.getContent()) {
            if (d.getEmployeeId() != null) employeeIds.add(d.getEmployeeId());
            if (d.getDecidedByEmployeeId() != null) employeeIds.add(d.getDecidedByEmployeeId());
        }
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);

        List<DisciplineResponse> responses = page.getContent().stream().map(d -> {
            DisciplineResponse res = disciplineMapper.toResponse(d);
            EmployeeSummary emp = empMap.get(d.getEmployeeId());
            res.setEmployee(emp);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            if (d.getDecidedByEmployeeId() != null) {
                EmployeeSummary decider = empMap.get(d.getDecidedByEmployeeId());
                res.setDecidedBy(decider);
                if (decider != null) {
                    res.setDecidedByEmployeeName(decider.getFullName());
                }
            }
            return res;
        }).toList();

        return PageData.of(page, responses);
    }


    @Transactional
    public void deleteDiscipline(UUID id) {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.DISCIPLINE_NOT_FOUND));
        if (discipline.getStatus() == DisciplineStatus.DECIDED) {
            throw new AppException(DisciplineErrorCode.DISCIPLINE_ALREADY_APPROVED);
        }
        disciplineRepository.delete(discipline);
    }


    @Transactional
    public DisciplineResponse decideDiscipline(UUID id, UUID currentUserId) {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.DISCIPLINE_NOT_FOUND));

        if (discipline.getStatus() == DisciplineStatus.DECIDED) {
            throw new AppException(DisciplineErrorCode.DISCIPLINE_ALREADY_APPROVED);
        }

        LocalDate now = LocalDate.now();
        if (now.isAfter(discipline.getStatuteOfLimitationDeadline())) {
            throw new AppException(DisciplineErrorCode.STATUTE_OF_LIMITATIONS_EXPIRED);
        }

        // Tuân thủ Điều 122 BLLĐ: Yêu cầu chứng minh lỗi, nội quy và thành phần cuộc họp
        validateDisciplineProcedures(discipline);

        // Theo Rule 367 & BA: Kỷ luật sa thải (SA_THAI) bắt buộc phải qua Workflow phê duyệt cấp cao trước khi ban hành
        if (discipline.getDisciplineType() == DisciplineType.SA_THAI) {
            throw new AppException(DisciplineErrorCode.TERMINATION_REQUIRES_APPROVAL,
                    "Kỷ luật sa thải nhân viên không được ban hành trực tiếp mà bắt buộc phải qua quy trình phê duyệt cấp cao");
        }

        return applyDecision(discipline, now, currentUserId);
    }


    @Transactional
    public DisciplineResponse approveDismissal(UUID id, UUID currentUserId) {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.DISCIPLINE_NOT_FOUND));

        if (discipline.getStatus() == DisciplineStatus.DECIDED) {
            throw new AppException(DisciplineErrorCode.DISCIPLINE_ALREADY_APPROVED);
        }

        if (discipline.getDisciplineType() != DisciplineType.SA_THAI) {
            throw AppException.badRequest("Quy trình phê duyệt sa thải chỉ áp dụng cho hình thức kỷ luật sa thải (SA_THAI)");
        }

        LocalDate now = LocalDate.now();
        if (now.isAfter(discipline.getStatuteOfLimitationDeadline())) {
            throw new AppException(DisciplineErrorCode.STATUTE_OF_LIMITATIONS_EXPIRED);
        }

        validateDisciplineProcedures(discipline);

        return applyDecision(discipline, now, currentUserId);
    }


    @Transactional(readOnly = true)
    public StatuteOfLimitationResponse checkStatuteOfLimitation(UUID id) {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.DISCIPLINE_NOT_FOUND));

        LocalDate now = LocalDate.now();
        LocalDate deadline = discipline.getStatuteOfLimitationDeadline();
        boolean isExpired = now.isAfter(deadline);
        long remainingDays = isExpired ? 0 : ChronoUnit.DAYS.between(now, deadline);

        String legalBasis = discipline.getViolationCategory() == ViolationCategory.FINANCIAL_ASSET_CONFIDENTIAL
                ? "Điều 123.1 BLLĐ 2019 (Thời hiệu tối đa 12 tháng đối với vi phạm tài chính, tài sản, bí mật công nghệ/kinh doanh)"
                : "Điều 123.1 BLLĐ 2019 (Thời hiệu tối đa 06 tháng đối với vi phạm thông thường)";

        String message = isExpired
                ? "Đã quá thời hiệu xử lý kỷ luật lao động. Không được phép ban hành quyết định kỷ luật đối với vi phạm này."
                : String.format("Còn trong thời hiệu xử lý kỷ luật lao động (còn %d ngày, hạn chót: %s).", remainingDays, deadline);

        EmployeeSummary emp = employeeService.getEmployeeSummary(discipline.getEmployeeId());

        return StatuteOfLimitationResponse.builder()
                .disciplineId(discipline.getId())
                .employeeId(discipline.getEmployeeId())
                .employeeName(emp != null ? emp.getFullName() : null)
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .violationDate(discipline.getViolationDate())
                .violationCategory(discipline.getViolationCategory())
                .statuteOfLimitationDeadline(deadline)
                .isExpired(isExpired)
                .remainingDays(remainingDays)
                .legalBasis(legalBasis)
                .message(message)
                .build();
    }


    @Transactional
    public int autoExpireDisciplines() {
        LocalDate today = LocalDate.now();
        List<Discipline> expiredList = disciplineRepository.findByStatusAndExpiryDateLessThanEqual(DisciplineStatus.DECIDED, today);
        for (Discipline d : expiredList) {
            d.setStatus(DisciplineStatus.EXPIRED);
            disciplineRepository.save(d);
            log.info("Auto-expired discipline id={} for employee id={} according to Article 126 Labour Code", d.getId(), d.getEmployeeId());
        }
        return expiredList.size();
    }

    private void validateDisciplineProcedures(Discipline discipline) {
        if (discipline.getHandbookReference() == null || discipline.getHandbookReference().isBlank()
                || discipline.getEvidenceFiles() == null || discipline.getEvidenceFiles().isBlank()
                || discipline.getMeetingDate() == null
                || discipline.getMeetingAttendees() == null || discipline.getMeetingAttendees().isBlank()) {
            throw new AppException(DisciplineErrorCode.DISCIPLINE_PROCEDURE_INCOMPLETE,
                    "Kỷ luật lao động bắt buộc phải có đầy đủ căn cứ nội quy, file chứng minh, ngày họp và thành phần tham dự theo Điều 122 BLLĐ");
        }
    }

    private DisciplineResponse applyDecision(Discipline discipline, LocalDate now, UUID currentUserId) {
        UUID deciderId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        discipline.setStatus(DisciplineStatus.DECIDED);
        discipline.setDecisionDate(now);
        discipline.setDecidedByEmployeeId(deciderId);

        if (discipline.getDisciplineType() == DisciplineType.KHIEN_TRACH) {
            discipline.setExpiryDate(now.plusMonths(3));
        } else if (discipline.getDisciplineType() == DisciplineType.KEO_DAI_NANG_LUONG || discipline.getDisciplineType() == DisciplineType.CACH_CHUC) {
            discipline.setExpiryDate(now.plusMonths(6));
        }

        discipline = disciplineRepository.save(discipline);

        // Kỷ luật sa thải (SA_THAI) tự động liên kết sang quy trình Offboarding & kích hoạt bàn giao tài sản
        if (discipline.getDisciplineType() == DisciplineType.SA_THAI) {
            CreateTerminationRequest termReq = CreateTerminationRequest.builder()
                    .employeeId(discipline.getEmployeeId())
                    .companyId(discipline.getCompanyId())
                    .lastWorkingDate(now)
                    .decisionReasonCategory(TerminationReason.DISCIPLINARY_DISMISSAL)
                    .disciplineId(discipline.getId())
                    .reasonDetail("Kỷ luật sa thải theo biên bản kỷ luật id=" + discipline.getId() + (discipline.getReason() != null ? ": " + discipline.getReason() : ""))
                    .build();
            offboardingService.createTermination(termReq, currentUserId);
            log.info("Automatically created Termination and initialized Offboarding clearances for dismissed employee id={}", discipline.getEmployeeId());
        }

        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(discipline.getEmployeeId());
        return toDisciplineResponse(discipline, employee);
    }

    // =========================================================================
    // 3. GRIEVANCES
    // =========================================================================

    @Transactional
    public GrievanceResponse createGrievance(CreateGrievanceRequest request, UUID currentUserId) {
        UUID employeeId;
        if (request.getEmployeeId() != null) {
            employeeId = request.getEmployeeId();
        } else {
            employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                    .orElseThrow(() -> new AppException(DisciplineErrorCode.CURRENT_USER_NOT_LINKED_EMPLOYEE));
        }

        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(employeeId);
        UUID companyId = request.getCompanyId() != null
                ? request.getCompanyId()
                : (employee.getCompany() != null ? employee.getCompany().getId() : null);

        Grievance grievance = Grievance.builder()
                .employeeId(employee.getId())
                .companyId(companyId)
                .submittedDate(LocalDate.now())
                .category(request.getCategory())
                .description(request.getDescription())
                .status(GrievanceStatus.OPEN)
                .build();

        grievance = grievanceRepository.save(grievance);
        return toGrievanceResponse(grievance, employee);
    }

    @Transactional
    public GrievanceResponse resolveGrievance(UUID id, ResolveGrievanceRequest request, UUID currentUserId) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.GRIEVANCE_NOT_FOUND));

        if (grievance.getStatus() == GrievanceStatus.RESOLVED || grievance.getStatus() == GrievanceStatus.CLOSED) {
            throw new AppException(DisciplineErrorCode.GRIEVANCE_ALREADY_RESOLVED);
        }

        if (request.getStatus() != GrievanceStatus.RESOLVED && request.getStatus() != GrievanceStatus.CLOSED) {
            throw AppException.badRequest("Trạng thái giải quyết khiếu nại chỉ được phép là RESOLVED hoặc CLOSED");
        }

        UUID resolverId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        grievance.setStatus(request.getStatus());
        grievance.setResolutionNote(request.getResolutionNote());
        grievance.setResolvedByEmployeeId(resolverId);
        grievance.setResolvedAt(LocalDateTime.now());

        grievance = grievanceRepository.save(grievance);
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(grievance.getEmployeeId());
        return toGrievanceResponse(grievance, employee);
    }

    @Transactional(readOnly = true)
    public GrievanceDetailResponse getGrievanceById(UUID id) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new AppException(DisciplineErrorCode.GRIEVANCE_NOT_FOUND));

        // Kiểm tra quyền: nếu user có scope OWN thì chỉ được xem đơn khiếu nại của chính mình
        if (!permEvaluator.has("grievance.manage") && !permEvaluator.has("discipline.manage")) {
            DataScope scope = permEvaluator.getDataScope("grievance.view").orElse(DataScope.OWN);
            if (scope == DataScope.OWN) {
                Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();
                if (currentUserIdOpt.isPresent()) {
                    UUID myEmpId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get()).orElse(null);
                    if (myEmpId == null || !myEmpId.equals(grievance.getEmployeeId())) {
                        throw new AppException(DisciplineErrorCode.UNAUTHORIZED_DISCIPLINE_ACCESS);
                    }
                }
            }
        }

        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(grievance.getEmployeeId());
        return toGrievanceDetailResponse(grievance, employee);
    }

    @Transactional(readOnly = true)
    public PageData<GrievanceResponse> getGrievances(GrievanceFilter filter) {
        if (filter == null) {
            filter = new GrievanceFilter();
        }

        applyGrievanceDataScope(filter);

        Pageable pageable = filter.toPageable("createdAt", ALLOWED_GRIEVANCE_SORT_FIELDS);
        Page<Grievance> page = grievanceRepository.searchGrievances(
                filter.getCompanyId(), filter.getEmployeeId(), filter.getStatus(), pageable
        );

        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> employeeIds = new HashSet<>();
        for (Grievance g : page.getContent()) {
            if (g.getEmployeeId() != null) employeeIds.add(g.getEmployeeId());
            if (g.getResolvedByEmployeeId() != null) employeeIds.add(g.getResolvedByEmployeeId());
        }
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);

        List<GrievanceResponse> responses = page.getContent().stream().map(g -> {
            GrievanceResponse res = disciplineMapper.toResponse(g);
            EmployeeSummary emp = empMap.get(g.getEmployeeId());
            res.setEmployee(emp);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            if (g.getResolvedByEmployeeId() != null) {
                EmployeeSummary resolver = empMap.get(g.getResolvedByEmployeeId());
                res.setResolvedBy(resolver);
                if (resolver != null) {
                    res.setResolvedByEmployeeName(resolver.getFullName());
                }
            }
            return res;
        }).toList();

        return PageData.of(page, responses);
    }

    // =========================================================================
    // Helper Methods & DataScope Filtering
    // =========================================================================

    private void applyRewardDataScope(RewardFilter filter) {
        DataScope scope = permEvaluator.getDataScope("reward.view").orElse(DataScope.OWN);
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

    private void applyDisciplineDataScope(DisciplineFilter filter) {
        DataScope scope = permEvaluator.getDataScope("discipline.view").orElse(DataScope.OWN);
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

    private void applyGrievanceDataScope(GrievanceFilter filter) {
        DataScope scope = permEvaluator.getDataScope("grievance.view").orElse(DataScope.OWN);
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

    private RewardResponse toRewardResponse(Reward reward, EmployeeDetailResponse employee) {
        RewardResponse res = disciplineMapper.toResponse(reward);
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
        if (reward.getDecidedByEmployeeId() != null) {
            EmployeeSummary decider = employeeService.getEmployeeSummary(reward.getDecidedByEmployeeId());
            res.setDecidedBy(decider);
            if (decider != null) {
                res.setDecidedByEmployeeName(decider.getFullName());
            }
        }
        return res;
    }

    private RewardDetailResponse toRewardDetailResponse(Reward reward, EmployeeDetailResponse employee) {
        RewardDetailResponse res = disciplineMapper.toDetailResponse(reward);
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
        if (reward.getDecidedByEmployeeId() != null) {
            EmployeeSummary decider = employeeService.getEmployeeSummary(reward.getDecidedByEmployeeId());
            res.setDecidedBy(decider);
            if (decider != null) {
                res.setDecidedByEmployeeName(decider.getFullName());
            }
        }
        return res;
    }

    private DisciplineResponse toDisciplineResponse(Discipline discipline, EmployeeDetailResponse employee) {
        DisciplineResponse res = disciplineMapper.toResponse(discipline);
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
        if (discipline.getDecidedByEmployeeId() != null) {
            EmployeeSummary decider = employeeService.getEmployeeSummary(discipline.getDecidedByEmployeeId());
            res.setDecidedBy(decider);
            if (decider != null) {
                res.setDecidedByEmployeeName(decider.getFullName());
            }
        }
        return res;
    }

    private DisciplineDetailResponse toDisciplineDetailResponse(Discipline discipline, EmployeeDetailResponse employee) {
        DisciplineDetailResponse res = disciplineMapper.toDetailResponse(discipline);
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
        if (discipline.getDecidedByEmployeeId() != null) {
            EmployeeSummary decider = employeeService.getEmployeeSummary(discipline.getDecidedByEmployeeId());
            res.setDecidedBy(decider);
            if (decider != null) {
                res.setDecidedByEmployeeName(decider.getFullName());
            }
        }
        return res;
    }

    private GrievanceResponse toGrievanceResponse(Grievance grievance, EmployeeDetailResponse employee) {
        GrievanceResponse res = disciplineMapper.toResponse(grievance);
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
        if (grievance.getResolvedByEmployeeId() != null) {
            EmployeeSummary resolver = employeeService.getEmployeeSummary(grievance.getResolvedByEmployeeId());
            res.setResolvedBy(resolver);
            if (resolver != null) {
                res.setResolvedByEmployeeName(resolver.getFullName());
            }
        }
        return res;
    }

    private GrievanceDetailResponse toGrievanceDetailResponse(Grievance grievance, EmployeeDetailResponse employee) {
        GrievanceDetailResponse res = disciplineMapper.toDetailResponse(grievance);
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
        if (grievance.getResolvedByEmployeeId() != null) {
            EmployeeSummary resolver = employeeService.getEmployeeSummary(grievance.getResolvedByEmployeeId());
            res.setResolvedBy(resolver);
            if (resolver != null) {
                res.setResolvedByEmployeeName(resolver.getFullName());
            }
        }
        return res;
    }
}

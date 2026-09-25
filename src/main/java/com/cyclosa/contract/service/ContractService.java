package com.cyclosa.contract.service;

import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.contract.dto.request.*;
import com.cyclosa.contract.dto.response.*;
import com.cyclosa.contract.engine.SeveranceCalculationEngine;
import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.entity.ContractAddendum;
import com.cyclosa.contract.entity.ContractTermination;
import com.cyclosa.contract.enums.AddendumType;
import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.enums.TerminationGround;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.mapper.ContractMapper;
import com.cyclosa.contract.repository.ContractAddendumRepository;
import com.cyclosa.contract.repository.ContractRepository;
import com.cyclosa.contract.repository.ContractTerminationRepository;
import com.cyclosa.contract.util.VietnameseNumberToWordsConverter;
import com.cyclosa.contract.validator.ContractLegalValidator;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractAddendumRepository addendumRepository;
    private final ContractTerminationRepository terminationRepository;
    private final EmployeeService employeeService;
    private final UserService userService;
    private final SecurityPermissionEvaluator perm;
    private final ContractLegalValidator legalValidator;
    private final SeveranceCalculationEngine severanceEngine;
    private final ContractMapper contractMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "contractNumber", "startDate", "endDate", "basicSalary", "createdAt"
    );
    @Transactional
    public ContractDetailResponse createContract(CreateContractRequest request) {
        EmployeeSummary employee = employeeService.getEmployeeSummary(request.getEmployeeId());
        if (employee == null) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }

        // 1. Kiểm tra pháp lý thời hạn (Điều 20)
        legalValidator.validateContractTypeAndDuration(request.getContractType(), request.getStartDate(), request.getEndDate());

        // 2. Kiểm tra giới hạn 02 lần HĐ xác định thời hạn (Điều 20.2)
        legalValidator.validateDefiniteContractRenewalLimit(request.getEmployeeId(), request.getCompanyId(), request.getContractType());

        // 3. Kiểm tra thử việc (Điều 24, 25, 26)
        if (request.getContractType() == ContractType.PROBATION) {
            legalValidator.validateProbationRules(
                    request.getEmployeeId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    60, // Mặc định 60 ngày cho trình độ cao đẳng/đại học
                    request.getBasicSalary(),
                    request.getBasicSalary() // Baseline
            );
        }

        // 4. Sinh số hợp đồng tự động nếu chưa truyền
        String contractNumber = request.getContractNumber();
        if (contractNumber == null || contractNumber.isBlank()) {
            contractNumber = generateContractNumber(request.getCompanyId(), request.getContractType());
        } else if (contractRepository.existsByContractNumberAndCompanyId(contractNumber, request.getCompanyId())) {
            throw new AppException(ContractErrorCode.CONTRACT_NUMBER_EXISTS);
        }

        // 5. Xác định thứ tự lần ký HĐ xác định thời hạn
        Integer sequence = null;
        if (request.getContractType() == ContractType.DEFINITE_TERM) {
            long existingCount = contractRepository.countDefiniteContracts(request.getEmployeeId(), request.getCompanyId(), ContractType.DEFINITE_TERM);
            sequence = (int) existingCount + 1;
        }

        Contract contract = contractMapper.toEntity(request);
        contract.setContractNumber(contractNumber);
        contract.setContractStatus(ContractStatus.DRAFT);
        contract.setDefiniteContractSequence(sequence);

        if (contract.getInsuranceSalary() == null) {
            contract.setInsuranceSalary(request.getBasicSalary());
        }

        Contract savedContract = contractRepository.save(contract);
        log.info("Created Contract id={}, number={}, status={}", savedContract.getId(), savedContract.getContractNumber(), savedContract.getContractStatus());

        return enrichDetailResponse(savedContract, employee);
    }
    @Transactional
    public ContractDetailResponse updateDraftContract(UUID id, UpdateContractRequest request) {
        Contract contract = findContract(id);
        if (contract.getContractStatus() != ContractStatus.DRAFT) {
            log.error("Không thể sửa hợp đồng id={} vì trạng thái là {}", id, contract.getContractStatus());
            throw new AppException(ContractErrorCode.CANNOT_MODIFY_ACTIVE_CONTRACT);
        }

        LocalDate newStart = request.getStartDate() != null ? request.getStartDate() : contract.getStartDate();
        LocalDate newEnd = request.getEndDate() != null ? request.getEndDate() : contract.getEndDate();

        legalValidator.validateContractTypeAndDuration(contract.getContractType(), newStart, newEnd);

        contract.setStartDate(newStart);
        contract.setEndDate(newEnd);
        if (request.getSignDate() != null) contract.setSignDate(request.getSignDate());
        if (request.getSignerEmployeeId() != null) contract.setSignerEmployeeId(request.getSignerEmployeeId());
        if (request.getBasicSalary() != null) contract.setBasicSalary(request.getBasicSalary());
        if (request.getInsuranceSalary() != null) contract.setInsuranceSalary(request.getInsuranceSalary());
        if (request.getAllowanceLunch() != null) contract.setAllowanceLunch(request.getAllowanceLunch());
        if (request.getAllowancePhone() != null) contract.setAllowancePhone(request.getAllowancePhone());
        if (request.getAllowanceTransport() != null) contract.setAllowanceTransport(request.getAllowanceTransport());
        if (request.getAllowanceOther() != null) contract.setAllowanceOther(request.getAllowanceOther());
        if (request.getWorkingHoursType() != null) contract.setWorkingHoursType(request.getWorkingHoursType());
        if (request.getWorkLocationAddress() != null) contract.setWorkLocationAddress(request.getWorkLocationAddress());
        if (request.getNote() != null) contract.setNote(request.getNote());

        Contract updated = contractRepository.save(contract);
        EmployeeSummary employee = employeeService.getEmployeeSummary(updated.getEmployeeId());
        return enrichDetailResponse(updated, employee);
    }
    @Transactional
    public ContractResponse submitContractForApproval(UUID id) {
        Contract contract = findContract(id);
        if (contract.getContractStatus() != ContractStatus.DRAFT) {
            throw new AppException(ContractErrorCode.INVALID_CONTRACT_STATUS_TRANSITION, "Chỉ hợp đồng DRAFT mới có thể gửi phê duyệt");
        }

        contract.setContractStatus(ContractStatus.PENDING_APPROVAL);
        Contract updated = contractRepository.save(contract);
        log.info("Contract id={} submitted for approval", id);
        return toContractResponse(updated);
    }
    @Transactional
    public ContractResponse approveContract(UUID id, UUID approverId) {
        Contract contract = findContract(id);
        if (contract.getContractStatus() != ContractStatus.PENDING_APPROVAL) {
            throw new AppException(ContractErrorCode.INVALID_CONTRACT_STATUS_TRANSITION, "Hợp đồng không ở trạng thái chờ duyệt");
        }

        contract.setContractStatus(ContractStatus.APPROVED);
        Contract updated = contractRepository.save(contract);
        log.info("Contract id={} approved by approver={}", id, approverId);
        return toContractResponse(updated);
    }
    @Transactional
    public ContractResponse activateContract(UUID id, LocalDate signDate, UUID signerEmployeeId, String signedContractUrl) {
        Contract contract = findContract(id);

        if (contract.getContractStatus() != ContractStatus.APPROVED && contract.getContractStatus() != ContractStatus.DRAFT) {
            throw new AppException(ContractErrorCode.INVALID_CONTRACT_STATUS_TRANSITION, "Hợp đồng phải ở trạng thái APPROVED hoặc DRAFT để kích hoạt hiệu lực");
        }

        // Chặn trùng hợp đồng ACTIVE
        legalValidator.validateNoExistingActiveContract(contract.getEmployeeId(), contract.getId());

        contract.setContractStatus(ContractStatus.ACTIVE);
        if (signDate != null) contract.setSignDate(signDate);
        if (signerEmployeeId != null) contract.setSignerEmployeeId(signerEmployeeId);
        if (signedContractUrl != null) contract.setSignedContractUrl(signedContractUrl);

        Contract savedContract = contractRepository.save(contract);

        // ĐỒNG BỘ: Chuyển trạng thái nhân viên sang ACTIVE nếu ký HĐ chính thức qua EmployeeService
        try {
            EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(contract.getEmployeeId());
            if (contract.getContractType() != ContractType.PROBATION && emp.getEmploymentStatus() == EmploymentStatus.PROBATION) {
                employeeService.updateEmploymentStatus(contract.getEmployeeId(), EmploymentStatus.ACTIVE);
                log.info("Automatically transitioned employee id={} to ACTIVE upon contract activation", emp.getId());
            }
        } catch (Exception e) {
            log.warn("Could not transition employee status upon contract activation: {}", e.getMessage());
        }

        log.info("Activated Contract id={}, number={}", savedContract.getId(), savedContract.getContractNumber());
        return toContractResponse(savedContract);
    }
    @Transactional
    public ContractAddendumResponse addAddendum(UUID contractId, CreateContractAddendumRequest request) {
        Contract contract = findContract(contractId);
        if (contract.getContractStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ContractErrorCode.INVALID_CONTRACT_STATUS_TRANSITION, "Chỉ có thể tạo phụ lục cho hợp đồng đang có hiệu lực (ACTIVE)");
        }

        String addendumNumber = request.getAddendumNumber();
        if (addendumNumber == null || addendumNumber.isBlank()) {
            int seq = contract.getAddenda().size() + 1;
            addendumNumber = String.format("PL-%02d/%s", seq, contract.getContractNumber());
        } else if (addendumRepository.existsByContractIdAndAddendumNumber(contractId, addendumNumber)) {
            throw new AppException(ContractErrorCode.ADDENDUM_NUMBER_EXISTS);
        }

        // Nếu sửa đổi thời hạn, kiểm tra không vượt quá 36 tháng đối với HĐ xác định thời hạn
        if (request.getAddendumType() == AddendumType.TERM_MODIFICATION && request.getNewEndDate() != null) {
            legalValidator.validateContractTypeAndDuration(contract.getContractType(), contract.getStartDate(), request.getNewEndDate());
            contract.setEndDate(request.getNewEndDate());
        }

        // Cập nhật mức lương mới vào hợp đồng nếu phụ lục điều chỉnh lương
        if (request.getAddendumType() == AddendumType.SALARY_ADJUSTMENT && request.getNewBasicSalary() != null) {
            contract.setBasicSalary(request.getNewBasicSalary());
            if (request.getNewAllowanceLunch() != null) contract.setAllowanceLunch(request.getNewAllowanceLunch());
            if (request.getNewAllowancePhone() != null) contract.setAllowancePhone(request.getNewAllowancePhone());
            if (request.getNewAllowanceTransport() != null) contract.setAllowanceTransport(request.getNewAllowanceTransport());
            if (request.getNewAllowanceOther() != null) contract.setAllowanceOther(request.getNewAllowanceOther());
        }

        ContractAddendum addendum = contractMapper.toAddendumEntity(request);
        addendum.setContract(contract);
        addendum.setAddendumNumber(addendumNumber);

        ContractAddendum savedAddendum = addendumRepository.save(addendum);
        contractRepository.save(contract);

        log.info("Added Addendum id={}, number={} to Contract id={}", savedAddendum.getId(), addendumNumber, contractId);
        return contractMapper.toAddendumResponse(savedAddendum);
    }
    @Transactional
    public ContractDetailResponse renewContract(UUID contractId, RenewContractRequest request) {
        Contract oldContract = findContract(contractId);

        // Kiểm tra Điều 20.2: Không được tái ký HĐ xác định thời hạn lần thứ 3
        legalValidator.validateDefiniteContractRenewalLimit(oldContract.getEmployeeId(), oldContract.getCompanyId(), request.getNewContractType());
        legalValidator.validateContractTypeAndDuration(request.getNewContractType(), request.getStartDate(), request.getEndDate());

        // Đánh dấu HĐ cũ là RENEWED
        oldContract.setContractStatus(ContractStatus.RENEWED);
        contractRepository.save(oldContract);

        // Tạo HĐ mới
        String newContractNumber = generateContractNumber(oldContract.getCompanyId(), request.getNewContractType());
        Integer sequence = null;
        if (request.getNewContractType() == ContractType.DEFINITE_TERM) {
            long existingCount = contractRepository.countDefiniteContracts(oldContract.getEmployeeId(), oldContract.getCompanyId(), ContractType.DEFINITE_TERM);
            sequence = (int) existingCount + 1;
        }

        Contract newContract = Contract.builder()
                .contractNumber(newContractNumber)
                .companyId(oldContract.getCompanyId())
                .employeeId(oldContract.getEmployeeId())
                .contractType(request.getNewContractType())
                .contractStatus(ContractStatus.DRAFT)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .signDate(request.getSignDate())
                .signerEmployeeId(request.getSignerEmployeeId())
                .basicSalary(request.getBasicSalary() != null ? request.getBasicSalary() : oldContract.getBasicSalary())
                .insuranceSalary(request.getInsuranceSalary() != null ? request.getInsuranceSalary() : oldContract.getInsuranceSalary())
                .allowanceLunch(request.getAllowanceLunch() != null ? request.getAllowanceLunch() : oldContract.getAllowanceLunch())
                .allowancePhone(request.getAllowancePhone() != null ? request.getAllowancePhone() : oldContract.getAllowancePhone())
                .allowanceTransport(request.getAllowanceTransport() != null ? request.getAllowanceTransport() : oldContract.getAllowanceTransport())
                .allowanceOther(request.getAllowanceOther() != null ? request.getAllowanceOther() : oldContract.getAllowanceOther())
                .workingHoursType(oldContract.getWorkingHoursType())
                .workLocationAddress(oldContract.getWorkLocationAddress())
                .definiteContractSequence(sequence)
                .note(request.getNote())
                .build();

        Contract saved = contractRepository.save(newContract);
        EmployeeSummary employee = employeeService.getEmployeeSummary(oldContract.getEmployeeId());
        log.info("Renewed Contract id={} -> new Contract id={}, number={}", contractId, saved.getId(), saved.getContractNumber());

        return enrichDetailResponse(saved, employee);
    }
    @Transactional
    public ContractTerminationResponse terminateContract(UUID contractId, TerminateContractRequest request) {
        Contract contract = findContract(contractId);
        if (contract.getContractStatus() == ContractStatus.TERMINATED) {
            throw new AppException(ContractErrorCode.CONTRACT_ALREADY_TERMINATED);
        }

        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(contract.getEmployeeId());

        // 1. Chặn Điều 37 & 137.3 (Cấm chấm dứt với thai sản, nuôi con < 12 tháng, ốm đau)
        legalValidator.validateTerminationRestrictions(request.getTerminationGround(), false, false, false);

        // 2. Kiểm tra thời hạn báo trước (Điều 35, 36)
        boolean validNotice = legalValidator.checkNoticePeriod(contract, request.getTerminationGround(),
                request.getNoticeDate(), request.getFinalWorkingDate());

        // 3. Tính toán trợ cấp thôi việc / mất việc làm (Điều 46, 47)
        var severanceCalc = severanceEngine.calculate(
                request.getTerminationGround(),
                employee.getHireDate(),
                request.getFinalWorkingDate(),
                0, // Giả định BHTN
                contract.getBasicSalary()
        );

        BigDecimal severance = request.getManualSeveranceAllowance() != null
                ? request.getManualSeveranceAllowance()
                : severanceCalc.getSeveranceAllowance();

        BigDecimal jobLoss = request.getManualLossOfWorkAllowance() != null
                ? request.getManualLossOfWorkAllowance()
                : severanceCalc.getLossOfWorkAllowance();

        ContractTermination termination = ContractTermination.builder()
                .contract(contract)
                .terminationGround(request.getTerminationGround())
                .decisionNumber(request.getDecisionNumber() != null ? request.getDecisionNumber() : "QD-TLHD/" + contract.getContractNumber())
                .noticeDate(request.getNoticeDate())
                .finalWorkingDate(request.getFinalWorkingDate())
                .severanceAllowance(severance)
                .lossOfWorkAllowance(jobLoss)
                .remainingLeavePay(request.getRemainingLeavePay() != null ? request.getRemainingLeavePay() : BigDecimal.ZERO)
                .compensationAmount(request.getCompensationAmount() != null ? request.getCompensationAmount() : BigDecimal.ZERO)
                .unlawfulTermination(!validNotice || request.getTerminationGround() == TerminationGround.EMPLOYEE_ILLEGAL || request.getTerminationGround() == TerminationGround.EMPLOYER_ILLEGAL)
                .reason(request.getReason())
                .approvedByEmployeeId(request.getApprovedByEmployeeId())
                .signedDecisionUrl(request.getSignedDecisionUrl())
                .build();

        ContractTermination savedTermination = terminationRepository.save(termination);

        // Cập nhật trạng thái hợp đồng sang TERMINATED
        contract.setContractStatus(ContractStatus.TERMINATED);
        contractRepository.save(contract);

        // ĐỒNG BỘ: Chuyển trạng thái Employee và khóa tài khoản User qua Public Service APIs
        EmploymentStatus targetStatus = (request.getTerminationGround() == TerminationGround.EMPLOYEE_REGULAR || request.getTerminationGround() == TerminationGround.MUTUAL_AGREEMENT)
                ? EmploymentStatus.RESIGNED
                : EmploymentStatus.TERMINATED;

        employeeService.updateEmploymentStatus(contract.getEmployeeId(), targetStatus);

        if (employee.getUserId() != null) {
            userService.updateUserStatus(employee.getUserId(), UserStatus.LOCKED);
            log.info("Locked User id={} upon contract termination of employee id={}", employee.getUserId(), employee.getId());
        }

        log.info("Terminated Contract id={}, ground={}, employeeId={}", contractId, request.getTerminationGround(), employee.getId());
        return contractMapper.toTerminationResponse(savedTermination);
    }
    @Transactional(readOnly = true)
    public SeveranceEstimateResponse estimateSeverance(UUID contractId, TerminationGround ground, LocalDate finalWorkingDate) {
        Contract contract = findContract(contractId);
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(contract.getEmployeeId());

        LocalDate targetDate = finalWorkingDate != null ? finalWorkingDate : LocalDate.now();
        var result = severanceEngine.calculate(ground, employee.getHireDate(), targetDate, 0, contract.getBasicSalary());

        String legalBasis = ground.isEligibleForJobLoss() ? "Điều 47 BLLĐ 2019 (Trợ cấp mất việc làm)" :
                ground.isEligibleForSeverance() ? "Điều 46 BLLĐ 2019 (Trợ cấp thôi việc)" : "Không thuộc diện hưởng trợ cấp luật định";

        return SeveranceEstimateResponse.builder()
                .terminationGround(ground)
                .terminationGroundDescription(ground.getDescription())
                .hireDate(employee.getHireDate())
                .finalWorkingDate(targetDate)
                .totalTenureYears(result.getTotalTenureYears())
                .bhtnYears(result.getBhtnYears())
                .qualifyingYears(result.getQualifyingYears())
                .averageSalary(result.getAverageSalary())
                .severanceAllowance(result.getSeveranceAllowance())
                .lossOfWorkAllowance(result.getLossOfWorkAllowance())
                .eligible(result.isEligible())
                .legalBasis(legalBasis)
                .note(result.getNote())
                .build();
    }
    @Transactional(readOnly = true)
    public ContractDetailResponse getContractById(UUID id) {
        Contract contract = findContract(id);
        EmployeeSummary employee = employeeService.getEmployeeSummary(contract.getEmployeeId());
        return enrichDetailResponse(contract, employee);
    }
    @Transactional(readOnly = true)
    public PageData<ContractResponse> getContracts(ContractFilter filter) {
        if (filter == null) {
            filter = new ContractFilter();
        }

        // Áp dụng bảo vệ dữ liệu theo DataScope (Mục 4.3)
        DataScope scope = perm.getDataScope("contract.view").orElse(DataScope.OWN);
        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();

        switch (scope) {
            case OWN -> {
                if (currentUserIdOpt.isPresent()) {
                    UUID myEmpId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get()).orElse(null);
                    filter.setEmployeeId(myEmpId);
                }
            }
            case ALL, COMPANY, TEAM, DEPARTMENT -> {
                // Giữ nguyên bộ lọc theo filter đầu vào
            }
        }

        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);

        Page<Contract> pageResult = contractRepository.search(
                kw, filter.getCompanyId(), filter.getEmployeeId(), filter.getStatus(), filter.getType(), pageable
        );

        // Chống N+1 query: Thu thập toàn bộ employeeId của trang và tải một lần duy nhất qua EmployeeService
        Set<UUID> employeeIds = pageResult.getContent().stream()
                .map(Contract::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> employeeMap = employeeService.getEmployeeSummaries(employeeIds);

        List<ContractResponse> items = pageResult.getContent().stream().map(c -> {
            ContractResponse res = contractMapper.toResponse(c);
            EmployeeSummary emp = employeeMap.get(c.getEmployeeId());
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            return res;
        }).toList();

        return PageData.of(pageResult, items);
    }
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByEmployee(UUID employeeId) {
        List<Contract> contracts = contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
        EmployeeSummary emp = employeeService.getEmployeeSummary(employeeId);
        return contracts.stream().map(c -> {
            ContractResponse res = contractMapper.toResponse(c);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            return res;
        }).toList();
    }
    @Transactional(readOnly = true)
    public List<ContractResponse> getExpiringContracts(UUID companyId, int withinDays) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(withinDays > 0 ? withinDays : 45);
        List<Contract> contracts = contractRepository.findExpiringContracts(startDate, endDate);
        List<Contract> filtered = contracts.stream()
                .filter(c -> companyId == null || Objects.equals(c.getCompanyId(), companyId))
                .toList();

        Set<UUID> employeeIds = filtered.stream().map(Contract::getEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);

        return filtered.stream().map(c -> {
            ContractResponse res = contractMapper.toResponse(c);
            EmployeeSummary emp = empMap.get(c.getEmployeeId());
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            return res;
        }).toList();
    }
    @Transactional
    public void deleteDraftContract(UUID id) {
        Contract contract = findContract(id);
        if (contract.getContractStatus() != ContractStatus.DRAFT) {
            throw new AppException(ContractErrorCode.CANNOT_MODIFY_ACTIVE_CONTRACT, "Chỉ có thể xóa hợp đồng ở trạng thái DRAFT");
        }
        contractRepository.delete(contract);
        log.info("Deleted DRAFT Contract id={}", id);
    }

    private Contract findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));
    }

    private ContractResponse toContractResponse(Contract contract) {
        ContractResponse res = contractMapper.toResponse(contract);
        EmployeeSummary emp = employeeService.getEmployeeSummary(contract.getEmployeeId());
        if (emp != null) {
            res.setEmployeeName(emp.getFullName());
            res.setEmployeeCode(emp.getEmployeeCode());
        }
        return res;
    }

    private ContractDetailResponse enrichDetailResponse(Contract contract, EmployeeSummary employee) {
        ContractDetailResponse res = contractMapper.toDetailResponse(contract);
        if (employee != null) {
            res.setEmployeeName(employee.getFullName());
            res.setEmployeeCode(employee.getEmployeeCode());
        }
        if (contract.getBasicSalary() != null) {
            res.setBasicSalaryInWords(VietnameseNumberToWordsConverter.convertToWords(contract.getBasicSalary()));
        }
        return res;
    }

    private String generateContractNumber(UUID companyId, ContractType contractType) {
        String typeCode = switch (contractType) {
            case INDEFINITE_TERM -> "KTH";
            case DEFINITE_TERM -> "CT";
            case PROBATION -> "TV";
        };
        int year = Year.now().getValue();
        long count = contractRepository.count() + 1;
        return String.format("CYC/%d/HDLD-%s/%05d", year, typeCode, count);
    }

    @Transactional(readOnly = true)
    public Optional<ContractResponse> getActiveContractByEmployee(UUID employeeId) {
        return contractRepository.findByEmployeeIdAndContractStatus(employeeId, ContractStatus.ACTIVE)
                .map(contractMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<UUID, ContractResponse> getActiveContractsByCompany(UUID companyId) {
        if (companyId == null) {
            return Collections.emptyMap();
        }
        return contractRepository.findByCompanyIdAndContractStatus(companyId, ContractStatus.ACTIVE).stream()
                .map(contractMapper::toResponse)
                .collect(Collectors.toMap(ContractResponse::getEmployeeId, c -> c, (a, b) -> a));
    }
}

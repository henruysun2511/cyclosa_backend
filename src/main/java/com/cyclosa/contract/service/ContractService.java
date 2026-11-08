package com.cyclosa.contract.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.contract.dto.ContractAppendixDto;
import com.cyclosa.contract.dto.ContractDto;
import com.cyclosa.contract.dto.ContractSigningDto;
import com.cyclosa.contract.entity.*;
import com.cyclosa.contract.entity.enums.*;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.repository.*;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.organization.entity.Company;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.DepartmentRepository;
import com.cyclosa.organization.repository.DepartmentVersionRepository;
import com.cyclosa.organization.repository.JobTitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractAllowanceRepository allowanceRepository;
    private final ContractAppendixRepository appendixRepository;
    private final ContractVersionRepository versionRepository;
    private final ContractSignerRepository signerRepository;

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentVersionRepository departmentVersionRepository;
    private final BranchRepository branchRepository;
    private final JobTitleRepository jobTitleRepository;
    private final ContractComplianceService complianceService;

    @Transactional(readOnly = true)
    public PageData<ContractDto.ContractSummaryResponse> getContracts(
            UUID companyId,
            UUID employeeId,
            ContractType contractType,
            ContractStatus status,
            Pageable pageable
    ) {
        Page<Contract> page = contractRepository.findByCompanyAndFilter(companyId, employeeId, contractType, status, pageable);
        return PageData.of(page, this::mapToSummaryResponse);
    }

    @Transactional(readOnly = true)
    public List<ContractDto.ContractSummaryResponse> getExpiringContracts(UUID companyId, int withinDays) {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(withinDays);
        return contractRepository.findExpiringContracts(companyId, fromDate, toDate).stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractDto.ContractSummaryResponse> getContractsByEmployee(UUID employeeId) {
        return contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId).stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractDto.ContractDetailResponse getContractById(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));
        return mapToDetailResponse(contract);
    }

    @Transactional
    public ContractDto.ContractDetailResponse createContract(ContractDto.CreateContractRequest request) {
        // Sinh mã hợp đồng duy nhất: HDLD-{YYYY}/{4 số ngẫu nhiên}
        String contractNo = generateContractNo();

        Contract contract = Contract.builder()
                .contractNo(contractNo)
                .employeeId(request.getEmployeeId())
                .employerCompanyId(request.getEmployerCompanyId())
                .employerRepresentativeId(request.getEmployerRepresentativeId())
                .templateVersionId(request.getTemplateVersionId())
                .contractType(request.getContractType())
                .previousContractId(request.getPreviousContractId())
                .sourceOfferId(request.getSourceOfferId())
                .jobTitleId(request.getJobTitleId())
                .departmentId(request.getDepartmentId())
                .branchId(request.getBranchId())
                .workLocation(request.getWorkLocation().trim())
                .workingTimeDescription(request.getWorkingTimeDescription().trim())
                .baseSalary(request.getBaseSalary())
                .insuranceSalary(request.getInsuranceSalary() != null ? request.getInsuranceSalary() : request.getBaseSalary())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : SalaryPaymentMethod.BANK_TRANSFER)
                .payDayOfMonth(request.getPayDayOfMonth() > 0 ? request.getPayDayOfMonth() : 5)
                .signingForm(request.getSigningForm() != null ? request.getSigningForm() : SigningForm.ELECTRONIC)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ContractStatus.DRAFT)
                .build();

        // Kiểm tra cơ bản
        if (contract.getContractType() != ContractType.INDEFINITE && contract.getEndDate() != null) {
            if (contract.getEndDate().isBefore(contract.getStartDate())) {
                throw new AppException(ContractErrorCode.INVALID_CONTRACT_DATES);
            }
        }

        Contract savedContract = contractRepository.save(contract);

        // Lưu danh mục phụ cấp
        if (request.getAllowances() != null && !request.getAllowances().isEmpty()) {
            List<ContractAllowance> allowances = request.getAllowances().stream()
                    .map(a -> ContractAllowance.builder()
                            .contract(savedContract)
                            .name(a.getName().trim())
                            .amount(a.getAmount())
                            .taxable(a.isTaxable())
                            .build())
                    .collect(Collectors.toList());
            allowanceRepository.saveAll(allowances);
        }

        // Tạo phiên bản văn bản PDF khởi tạo (Version 1)
        String mockPdfUrl = "https://storage.cyclosa.vn/contracts/" + contractNo + "-v1.pdf";
        String sha256 = generateMockHash(contractNo + savedContract.getId().toString());

        ContractVersion version1 = ContractVersion.builder()
                .contract(savedContract)
                .versionNo(1)
                .fileUrl(mockPdfUrl)
                .sha256Hash(sha256)
                .changeReason("Khởi tạo dự thảo hợp đồng")
                .build();
        ContractVersion savedVersion = versionRepository.save(version1);

        // Tạo 2 bên ký mặc định
        // Bên 1: Người lao động
        ContractSigner employeeSigner = ContractSigner.builder()
                .contractVersion(savedVersion)
                .signerEmployeeId(request.getEmployeeId())
                .signerSide(SignerSide.EMPLOYEE)
                .signingOrder(1)
                .signingMethod(request.getSigningForm() == SigningForm.PAPER ? SigningMethod.WET_INK : SigningMethod.OTP_CONFIRMATION)
                .status(SignerStatus.PENDING)
                .transactionId("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        // Bên 2: Người sử dụng lao động (Đại diện)
        ContractSigner employerSigner = ContractSigner.builder()
                .contractVersion(savedVersion)
                .signerEmployeeId(request.getEmployerRepresentativeId())
                .signerSide(SignerSide.EMPLOYER)
                .signingOrder(2)
                .signingMethod(request.getSigningForm() == SigningForm.PAPER ? SigningMethod.WET_INK : SigningMethod.DIGITAL_SIGNATURE)
                .status(SignerStatus.PENDING)
                .transactionId("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        signerRepository.saveAll(List.of(employeeSigner, employerSigner));

        return getContractById(savedContract.getId());
    }

    @Transactional
    public ContractDto.ContractDetailResponse updateContract(UUID id, ContractDto.UpdateContractRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new AppException(ContractErrorCode.CONTRACT_NOT_IN_DRAFT);
        }

        if (request.getWorkLocation() != null) contract.setWorkLocation(request.getWorkLocation().trim());
        if (request.getWorkingTimeDescription() != null) contract.setWorkingTimeDescription(request.getWorkingTimeDescription().trim());
        if (request.getBaseSalary() != null) contract.setBaseSalary(request.getBaseSalary());
        if (request.getInsuranceSalary() != null) contract.setInsuranceSalary(request.getInsuranceSalary());
        if (request.getPaymentMethod() != null) contract.setPaymentMethod(request.getPaymentMethod());
        if (request.getPayDayOfMonth() != null) contract.setPayDayOfMonth(request.getPayDayOfMonth());
        if (request.getSigningForm() != null) contract.setSigningForm(request.getSigningForm());
        if (request.getStartDate() != null) contract.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) contract.setEndDate(request.getEndDate());

        Contract saved = contractRepository.save(contract);

        if (request.getAllowances() != null) {
            allowanceRepository.deleteByContractId(id);
            List<ContractAllowance> allowances = request.getAllowances().stream()
                    .map(a -> ContractAllowance.builder()
                            .contract(saved)
                            .name(a.getName().trim())
                            .amount(a.getAmount())
                            .taxable(a.isTaxable())
                            .build())
                    .collect(Collectors.toList());
            allowanceRepository.saveAll(allowances);
        }

        return getContractById(id);
    }

    @Transactional
    public void submitContract(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new AppException(ContractErrorCode.CONTRACT_NOT_IN_DRAFT);
        }

        // Tự động kiểm tra tuân thủ trước khi trình
        ContractDto.ComplianceCheckResponse check = complianceService.checkCompliance(contract);
        if (!check.isCompliant()) {
            throw new AppException(ContractErrorCode.PROBATION_SALARY_BELOW_LEGAL_MIN,
                    "Không thể trình duyệt do vi phạm luật lao động: " + String.join(", ", check.getViolations()));
        }

        contract.setStatus(ContractStatus.PENDING_APPROVAL);
        contractRepository.save(contract);
    }

    @Transactional
    public void sendForSignature(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));

        contract.setStatus(ContractStatus.PENDING_SIGNATURE);
        contractRepository.save(contract);
    }

    @Transactional
    public void cancelContract(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() == ContractStatus.ACTIVE) {
            throw new AppException(ContractErrorCode.CANNOT_CANCEL_COMPLETED_CONTRACT);
        }

        contract.setStatus(ContractStatus.CANCELLED);
        contractRepository.save(contract);
    }

    @Transactional
    public ContractDto.ContractDetailResponse renewContract(UUID id) {
        Contract previous = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));

        long fixedTermCount = contractRepository.countFixedTermContractsByEmployee(previous.getEmployeeId());
        ContractType nextType = fixedTermCount >= 2 ? ContractType.INDEFINITE : ContractType.FIXED_TERM;

        LocalDate nextStart = previous.getEndDate() != null ? previous.getEndDate().plusDays(1) : LocalDate.now();
        LocalDate nextEnd = nextType == ContractType.INDEFINITE ? null : nextStart.plusYears(1).minusDays(1);

        ContractDto.CreateContractRequest renewReq = ContractDto.CreateContractRequest.builder()
                .employeeId(previous.getEmployeeId())
                .employerCompanyId(previous.getEmployerCompanyId())
                .employerRepresentativeId(previous.getEmployerRepresentativeId())
                .templateVersionId(previous.getTemplateVersionId())
                .contractType(nextType)
                .previousContractId(previous.getId())
                .jobTitleId(previous.getJobTitleId())
                .departmentId(previous.getDepartmentId())
                .branchId(previous.getBranchId())
                .workLocation(previous.getWorkLocation())
                .workingTimeDescription(previous.getWorkingTimeDescription())
                .baseSalary(previous.getBaseSalary())
                .insuranceSalary(previous.getInsuranceSalary())
                .paymentMethod(previous.getPaymentMethod())
                .payDayOfMonth(previous.getPayDayOfMonth())
                .signingForm(previous.getSigningForm())
                .startDate(nextStart)
                .endDate(nextEnd)
                .build();

        return createContract(renewReq);
    }

    @Transactional
    public void archiveContract(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));
        contract.setArchivedDocumentId(UUID.randomUUID());
        contractRepository.save(contract);
    }

    private String generateContractNo() {
        int year = Year.now().getValue();
        int randomNum = 1000 + new Random().nextInt(9000);
        return "HDLD-" + year + "/" + randomNum;
    }

    private String generateMockHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString().replace("-", "") + "00000000000000000000000000000000".substring(0, 32);
        }
    }

    public ContractDto.ContractSummaryResponse mapToSummaryResponse(Contract c) {
        Employee emp = employeeRepository.findById(c.getEmployeeId()).orElse(null);
        Company company = companyRepository.findById(c.getEmployerCompanyId()).orElse(null);
        String jobTitleName = jobTitleRepository.findById(c.getJobTitleId()).map(j -> j.getName()).orElse(null);
        String deptName = departmentVersionRepository.findLatestVersion(c.getDepartmentId()).map(v -> v.getName()).orElse(null);
        String branchName = branchRepository.findById(c.getBranchId()).map(b -> b.getName()).orElse(null);

        Integer daysUntilExpiry = null;
        if (c.getEndDate() != null && c.getStatus() == ContractStatus.ACTIVE) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), c.getEndDate());
            daysUntilExpiry = (int) days;
        }

        return ContractDto.ContractSummaryResponse.builder()
                .id(c.getId())
                .contractNo(c.getContractNo())
                .employeeId(c.getEmployeeId())
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .employeeName(emp != null ? emp.getFullName() : null)
                .employerCompanyId(c.getEmployerCompanyId())
                .companyName(company != null ? company.getName() : null)
                .contractType(c.getContractType())
                .jobTitleName(jobTitleName)
                .departmentName(deptName)
                .branchName(branchName)
                .baseSalary(c.getBaseSalary())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus())
                .signingForm(c.getSigningForm())
                .daysUntilExpiry(daysUntilExpiry)
                .createdAt(c.getCreatedAt())
                .build();
    }

    public ContractDto.ContractDetailResponse mapToDetailResponse(Contract c) {
        Employee emp = employeeRepository.findById(c.getEmployeeId()).orElse(null);
        Employee rep = employeeRepository.findById(c.getEmployerRepresentativeId()).orElse(null);
        Company company = companyRepository.findById(c.getEmployerCompanyId()).orElse(null);

        String jobTitleName = jobTitleRepository.findById(c.getJobTitleId()).map(j -> j.getName()).orElse(null);
        String deptName = departmentVersionRepository.findLatestVersion(c.getDepartmentId()).map(v -> v.getName()).orElse(null);
        String branchName = branchRepository.findById(c.getBranchId()).map(b -> b.getName()).orElse(null);

        Integer daysUntilExpiry = null;
        if (c.getEndDate() != null && c.getStatus() == ContractStatus.ACTIVE) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), c.getEndDate());
            daysUntilExpiry = (int) days;
        }

        List<ContractDto.AllowanceDto> allowances = allowanceRepository.findByContractId(c.getId()).stream()
                .map(a -> ContractDto.AllowanceDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .amount(a.getAmount())
                        .taxable(a.isTaxable())
                        .build())
                .collect(Collectors.toList());

        List<ContractVersion> versions = versionRepository.findByContractIdOrderByVersionNoDesc(c.getId());
        String latestFileUrl = !versions.isEmpty() ? versions.get(0).getFileUrl() : null;
        String latestHash = !versions.isEmpty() ? versions.get(0).getSha256Hash() : null;

        List<ContractSigningDto.ContractSignerResponse> signers = new ArrayList<>();
        if (!versions.isEmpty()) {
            signers = signerRepository.findByContractVersionIdOrderBySigningOrderAsc(versions.get(0).getId()).stream()
                    .map(s -> {
                        Employee sEmp = employeeRepository.findById(s.getSignerEmployeeId()).orElse(null);
                        return ContractSigningDto.ContractSignerResponse.builder()
                                .id(s.getId())
                                .contractVersionId(s.getContractVersion().getId())
                                .signerEmployeeId(s.getSignerEmployeeId())
                                .signerName(sEmp != null ? sEmp.getFullName() : null)
                                .signerEmail(sEmp != null ? sEmp.getPersonalEmail() : null)
                                .signerSide(s.getSignerSide())
                                .signingOrder(s.getSigningOrder())
                                .signingMethod(s.getSigningMethod())
                                .status(s.getStatus())
                                .signedAt(s.getSignedAt())
                                .declineReason(s.getDeclineReason())
                                .transactionId(s.getTransactionId())
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        return ContractDto.ContractDetailResponse.builder()
                .id(c.getId())
                .contractNo(c.getContractNo())
                .employeeId(c.getEmployeeId())
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .employeeName(emp != null ? emp.getFullName() : null)
                .employeePhone(emp != null ? emp.getPhone() : null)
                .employerCompanyId(c.getEmployerCompanyId())
                .companyName(company != null ? company.getName() : null)
                .companyTaxCode(company != null ? company.getTaxCode() : null)
                .companyAddress(company != null ? company.getAddress() : null)
                .employerRepresentativeId(c.getEmployerRepresentativeId())
                .representativeName(rep != null ? rep.getFullName() : null)
                .templateVersionId(c.getTemplateVersionId())
                .contractType(c.getContractType())
                .previousContractId(c.getPreviousContractId())
                .jobTitleId(c.getJobTitleId())
                .jobTitleName(jobTitleName)
                .departmentId(c.getDepartmentId())
                .departmentName(deptName)
                .branchId(c.getBranchId())
                .branchName(branchName)
                .workLocation(c.getWorkLocation())
                .workingTimeDescription(c.getWorkingTimeDescription())
                .baseSalary(c.getBaseSalary())
                .insuranceSalary(c.getInsuranceSalary())
                .paymentMethod(c.getPaymentMethod())
                .payDayOfMonth(c.getPayDayOfMonth())
                .signingForm(c.getSigningForm())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .signedDate(c.getSignedDate())
                .status(c.getStatus())
                .archivedDocumentId(c.getArchivedDocumentId())
                .daysUntilExpiry(daysUntilExpiry)
                .allowances(allowances)
                .signers(signers)
                .latestFileUrl(latestFileUrl)
                .latestFileHash(latestHash)
                .createdAt(c.getCreatedAt())
                .build();
    }
}

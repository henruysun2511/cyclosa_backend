package com.cyclosa.contract.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.contract.dto.ContractAppendixDto;
import com.cyclosa.contract.entity.*;
import com.cyclosa.contract.entity.enums.ContractStatus;
import com.cyclosa.contract.entity.enums.SignerSide;
import com.cyclosa.contract.entity.enums.SignerStatus;
import com.cyclosa.contract.entity.enums.SigningMethod;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.repository.*;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.JobTitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractAppendixService {

    private final ContractAppendixRepository appendixRepository;
    private final ContractAppendixAllowanceRepository appendixAllowanceRepository;
    private final ContractRepository contractRepository;
    private final ContractVersionRepository versionRepository;
    private final ContractSignerRepository signerRepository;
    private final JobTitleRepository jobTitleRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public List<ContractAppendixDto.ContractAppendixResponse> getAppendices(UUID contractId) {
        return appendixRepository.findByContractIdOrderByAppendixNoAsc(contractId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractAppendixDto.ContractAppendixResponse getAppendixById(UUID appendixId) {
        ContractAppendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new AppException(ContractErrorCode.APPENDIX_NOT_FOUND));
        return mapToResponse(appendix);
    }

    @Transactional
    public ContractAppendixDto.ContractAppendixResponse createAppendix(UUID contractId, ContractAppendixDto.CreateAppendixRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));

        int nextNo = appendixRepository.findMaxAppendixNoByContractId(contractId).orElse(0) + 1;

        ContractAppendix appendix = ContractAppendix.builder()
                .contract(contract)
                .appendixNo(nextNo)
                .templateVersionId(request.getTemplateVersionId())
                .effectiveDate(request.getEffectiveDate())
                .description(request.getDescription().trim())
                .newBaseSalary(request.getNewBaseSalary())
                .newInsuranceSalary(request.getNewInsuranceSalary())
                .newJobTitleId(request.getNewJobTitleId())
                .newBranchId(request.getNewBranchId())
                .newWorkLocation(request.getNewWorkLocation())
                .status(ContractStatus.DRAFT)
                .build();

        ContractAppendix saved = appendixRepository.save(appendix);

        // Lưu phụ cấp thay đổi nếu có
        if (request.getAllowances() != null && !request.getAllowances().isEmpty()) {
            List<ContractAppendixAllowance> allowances = request.getAllowances().stream()
                    .map(a -> ContractAppendixAllowance.builder()
                            .appendix(saved)
                            .name(a.getName().trim())
                            .amount(a.getAmount())
                            .taxable(a.isTaxable())
                            .build())
                    .collect(Collectors.toList());
            appendixAllowanceRepository.saveAll(allowances);
        }

        // Tạo ContractVersion cho phụ lục này
        String mockPdfUrl = "https://storage.cyclosa.vn/contracts/" + contract.getContractNo() + "-appendix-" + nextNo + ".pdf";
        ContractVersion version = ContractVersion.builder()
                .contract(contract)
                .appendixId(saved.getId())
                .versionNo(nextNo)
                .fileUrl(mockPdfUrl)
                .sha256Hash(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""))
                .changeReason("Phụ lục hợp đồng số " + nextNo)
                .build();
        ContractVersion savedVersion = versionRepository.save(version);

        // Tạo bên ký
        ContractSigner employeeSigner = ContractSigner.builder()
                .contractVersion(savedVersion)
                .signerEmployeeId(contract.getEmployeeId())
                .signerSide(SignerSide.EMPLOYEE)
                .signingOrder(1)
                .signingMethod(SigningMethod.OTP_CONFIRMATION)
                .status(SignerStatus.PENDING)
                .transactionId("TX-APX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        ContractSigner employerSigner = ContractSigner.builder()
                .contractVersion(savedVersion)
                .signerEmployeeId(contract.getEmployerRepresentativeId())
                .signerSide(SignerSide.EMPLOYER)
                .signingOrder(2)
                .signingMethod(SigningMethod.DIGITAL_SIGNATURE)
                .status(SignerStatus.PENDING)
                .transactionId("TX-APX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        signerRepository.saveAll(List.of(employeeSigner, employerSigner));

        return mapToResponse(saved);
    }

    @Transactional
    public ContractAppendixDto.ContractAppendixResponse updateAppendix(UUID appendixId, ContractAppendixDto.UpdateAppendixRequest request) {
        ContractAppendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new AppException(ContractErrorCode.APPENDIX_NOT_FOUND));

        if (appendix.getStatus() != ContractStatus.DRAFT) {
            throw new AppException(ContractErrorCode.CONTRACT_NOT_IN_DRAFT);
        }

        if (request.getEffectiveDate() != null) appendix.setEffectiveDate(request.getEffectiveDate());
        if (request.getDescription() != null) appendix.setDescription(request.getDescription().trim());
        if (request.getNewBaseSalary() != null) appendix.setNewBaseSalary(request.getNewBaseSalary());
        if (request.getNewInsuranceSalary() != null) appendix.setNewInsuranceSalary(request.getNewInsuranceSalary());
        if (request.getNewJobTitleId() != null) appendix.setNewJobTitleId(request.getNewJobTitleId());
        if (request.getNewBranchId() != null) appendix.setNewBranchId(request.getNewBranchId());
        if (request.getNewWorkLocation() != null) appendix.setNewWorkLocation(request.getNewWorkLocation().trim());

        return mapToResponse(appendixRepository.save(appendix));
    }

    @Transactional
    public void submitAppendix(UUID appendixId) {
        ContractAppendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new AppException(ContractErrorCode.APPENDIX_NOT_FOUND));
        appendix.setStatus(ContractStatus.PENDING_APPROVAL);
        appendixRepository.save(appendix);
    }

    @Transactional
    public void sendForSignature(UUID appendixId) {
        ContractAppendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new AppException(ContractErrorCode.APPENDIX_NOT_FOUND));
        appendix.setStatus(ContractStatus.PENDING_SIGNATURE);
        appendixRepository.save(appendix);
    }

    @Transactional
    public void cancelAppendix(UUID appendixId) {
        ContractAppendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new AppException(ContractErrorCode.APPENDIX_NOT_FOUND));
        appendix.setStatus(ContractStatus.CANCELLED);
        appendixRepository.save(appendix);
    }

    @Transactional
    public void archiveAppendix(UUID appendixId) {
        ContractAppendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new AppException(ContractErrorCode.APPENDIX_NOT_FOUND));
        appendix.setArchivedDocumentId(UUID.randomUUID());
        appendixRepository.save(appendix);
    }

    public ContractAppendixDto.ContractAppendixResponse mapToResponse(ContractAppendix a) {
        String jobTitleName = a.getNewJobTitleId() != null
                ? jobTitleRepository.findById(a.getNewJobTitleId()).map(j -> j.getName()).orElse(null)
                : null;
        String branchName = a.getNewBranchId() != null
                ? branchRepository.findById(a.getNewBranchId()).map(b -> b.getName()).orElse(null)
                : null;

        List<ContractAppendixDto.AppendixAllowanceDto> allowances = appendixAllowanceRepository.findByAppendixId(a.getId()).stream()
                .map(al -> ContractAppendixDto.AppendixAllowanceDto.builder()
                        .id(al.getId())
                        .name(al.getName())
                        .amount(al.getAmount())
                        .taxable(al.isTaxable())
                        .build())
                .collect(Collectors.toList());

        return ContractAppendixDto.ContractAppendixResponse.builder()
                .id(a.getId())
                .contractId(a.getContract().getId())
                .appendixNo(a.getAppendixNo())
                .effectiveDate(a.getEffectiveDate())
                .description(a.getDescription())
                .newBaseSalary(a.getNewBaseSalary())
                .newInsuranceSalary(a.getNewInsuranceSalary())
                .newJobTitleId(a.getNewJobTitleId())
                .newJobTitleName(jobTitleName)
                .newBranchId(a.getNewBranchId())
                .newBranchName(branchName)
                .newWorkLocation(a.getNewWorkLocation())
                .status(a.getStatus())
                .archivedDocumentId(a.getArchivedDocumentId())
                .allowances(allowances)
                .createdAt(a.getCreatedAt())
                .build();
    }
}


package com.cyclosa.contract.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.contract.dto.request.ContractTemplateRequest;
import com.cyclosa.contract.dto.response.ContractTemplateResponse;
import com.cyclosa.contract.engine.ContractTemplateEngine;
import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.entity.ContractTemplate;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.mapper.ContractMapper;
import com.cyclosa.contract.repository.ContractRepository;
import com.cyclosa.contract.repository.ContractTemplateRepository;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractTemplateService {

    private final ContractTemplateRepository templateRepository;
    private final ContractRepository contractRepository;
    private final EmployeeService employeeService;
    private final ContractTemplateEngine templateEngine;
    private final ContractMapper contractMapper;
    @Transactional
    public ContractTemplateResponse createTemplate(ContractTemplateRequest request) {
        if (templateRepository.existsByTemplateCodeAndCompanyId(request.getTemplateCode(), request.getCompanyId())) {
            throw new AppException(ContractErrorCode.TEMPLATE_CODE_EXISTS);
        }

        ContractTemplate template = contractMapper.toTemplateEntity(request);
        template.setVersion(1);
        template.setActive(true);

        ContractTemplate saved = templateRepository.save(template);
        log.info("Created ContractTemplate id={}, code={}, companyId={}", saved.getId(), saved.getTemplateCode(), saved.getCompanyId());
        return contractMapper.toTemplateResponse(saved);
    }
    @Transactional(readOnly = true)
    public ContractTemplateResponse getTemplateById(UUID id) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));
        return contractMapper.toTemplateResponse(template);
    }
    @Transactional(readOnly = true)
    public List<ContractTemplateResponse> getActiveTemplates(UUID companyId) {
        List<ContractTemplate> templates = templateRepository.findByCompanyIdAndActiveTrue(companyId);
        return contractMapper.toTemplateResponseList(templates);
    }
    @Transactional(readOnly = true)
    public ContractTemplateResponse getTemplateByCode(String templateCode, UUID companyId) {
        ContractTemplate template = templateRepository.findByTemplateCodeAndCompanyId(templateCode, companyId)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));
        return contractMapper.toTemplateResponse(template);
    }
    @Transactional(readOnly = true)
    public String renderContractDocument(UUID contractId, UUID templateId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));

        ContractTemplate template;
        if (templateId != null) {
            template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));
        } else {
            template = templateRepository.findFirstByCompanyIdAndContractTypeAndActiveTrueOrderByVersionDesc(
                            contract.getCompanyId(), contract.getContractType())
                    .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND,
                            "Không tìm thấy mẫu hợp đồng mặc định cho loại " + contract.getContractType().getDescription()));
        }

        EmployeeDetailResponse employee = null;
        try {
            employee = employeeService.getEmployeeByIdInternal(contract.getEmployeeId());
        } catch (Exception e) {
            log.warn("Could not find employee for contract id={}", contractId);
        }

        String empName = employee != null ? employee.getFullName() : "";
        String empCode = employee != null ? employee.getEmployeeCode() : "";

        String nationalId = employee != null && employee.getNationalIdNumber() != null ? employee.getNationalIdNumber() : "";
        String nationalIdDate = employee != null && employee.getNationalIdIssueDate() != null ? employee.getNationalIdIssueDate().toString() : "";
        String nationalIdPlace = employee != null && employee.getNationalIdIssuePlace() != null ? employee.getNationalIdIssuePlace() : "";
        String permAddr = employee != null && employee.getPermanentAddress() != null ? employee.getPermanentAddress() : "";
        String currAddr = employee != null && employee.getCurrentAddress() != null ? employee.getCurrentAddress() : "";
        String phone = employee != null && employee.getPhone() != null ? employee.getPhone() : "";
        String positionName = employee != null && employee.getPosition() != null ? employee.getPosition().getName() : "Chuyên viên";
        String orgUnitName = employee != null && employee.getOrganizationalUnit() != null ? employee.getOrganizationalUnit().getName() : "Phòng ban";

        Map<String, String> vars = templateEngine.buildStandardVariables(
                contract, empName, empCode, nationalId, nationalIdDate, nationalIdPlace,
                permAddr, currAddr, phone, positionName, orgUnitName,
                "CÔNG TY CỔ PHẦN CYCLOSA", "0109999888", "Hà Nội, Việt Nam",
                "Tổng Giám Đốc", "Người đại diện theo pháp luật"
        );

        return templateEngine.render(template.getTemplateContent(), vars);
    }
}

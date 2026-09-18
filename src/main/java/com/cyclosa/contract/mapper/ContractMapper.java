package com.cyclosa.contract.mapper;

import com.cyclosa.contract.dto.request.ContractTemplateRequest;
import com.cyclosa.contract.dto.request.CreateContractAddendumRequest;
import com.cyclosa.contract.dto.request.CreateContractRequest;
import com.cyclosa.contract.dto.response.*;
import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.entity.ContractAddendum;
import com.cyclosa.contract.entity.ContractTemplate;
import com.cyclosa.contract.entity.ContractTermination;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "contractStatus", ignore = true)
    @Mapping(target = "definiteContractSequence", ignore = true)
    @Mapping(target = "workflowInstanceId", ignore = true)
    @Mapping(target = "signedContractUrl", ignore = true)
    @Mapping(target = "addenda", ignore = true)
    @Mapping(target = "termination", ignore = true)
    Contract toEntity(CreateContractRequest request);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "contractTypeDescription", source = "contractType.description")
    @Mapping(target = "contractStatusDescription", source = "contractStatus.description")
    ContractResponse toResponse(Contract contract);

    List<ContractResponse> toResponseList(List<Contract> contracts);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "employeeDepartment", ignore = true)
    @Mapping(target = "employeePosition", ignore = true)
    @Mapping(target = "contractTypeDescription", source = "contractType.description")
    @Mapping(target = "contractStatusDescription", source = "contractStatus.description")
    @Mapping(target = "workingHoursDescription", source = "workingHoursType.description")
    @Mapping(target = "basicSalaryInWords", ignore = true)
    @Mapping(target = "totalGrossSalary", expression = "java(calculateTotalGross(contract))")
    ContractDetailResponse toDetailResponse(Contract contract);

    @Mapping(target = "contract", ignore = true)
    ContractAddendum toAddendumEntity(CreateContractAddendumRequest request);

    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "addendumTypeDescription", source = "addendumType.description")
    ContractAddendumResponse toAddendumResponse(ContractAddendum addendum);

    List<ContractAddendumResponse> toAddendumResponseList(List<ContractAddendum> addenda);

    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "terminationGroundDescription", source = "terminationGround.description")
    @Mapping(target = "totalPayout", expression = "java(calculateTotalPayout(termination))")
    ContractTerminationResponse toTerminationResponse(ContractTermination termination);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    ContractTemplate toTemplateEntity(ContractTemplateRequest request);

    @Mapping(target = "contractTypeDescription", source = "contractType.description")
    ContractTemplateResponse toTemplateResponse(ContractTemplate template);

    List<ContractTemplateResponse> toTemplateResponseList(List<ContractTemplate> templates);

    default BigDecimal calculateTotalGross(Contract contract) {
        BigDecimal total = contract.getBasicSalary() != null ? contract.getBasicSalary() : BigDecimal.ZERO;
        if (contract.getAllowanceLunch() != null) total = total.add(contract.getAllowanceLunch());
        if (contract.getAllowancePhone() != null) total = total.add(contract.getAllowancePhone());
        if (contract.getAllowanceTransport() != null) total = total.add(contract.getAllowanceTransport());
        if (contract.getAllowanceOther() != null) total = total.add(contract.getAllowanceOther());
        return total;
    }

    default BigDecimal calculateTotalPayout(ContractTermination termination) {
        if (termination == null) return BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        if (termination.getSeveranceAllowance() != null) total = total.add(termination.getSeveranceAllowance());
        if (termination.getLossOfWorkAllowance() != null) total = total.add(termination.getLossOfWorkAllowance());
        if (termination.getRemainingLeavePay() != null) total = total.add(termination.getRemainingLeavePay());
        if (termination.getCompensationAmount() != null) total = total.add(termination.getCompensationAmount());
        return total;
    }
}

package com.cyclosa.offboarding.mapper;

import com.cyclosa.offboarding.dto.response.*;
import com.cyclosa.offboarding.entity.ExitInterview;
import com.cyclosa.offboarding.entity.OffboardingClearance;
import com.cyclosa.offboarding.entity.Resignation;
import com.cyclosa.offboarding.entity.Termination;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OffboardingMapper {

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "approvedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "approvedByEmployee", ignore = true)
    ResignationResponse toResponse(Resignation resignation);

    List<ResignationResponse> toResignationResponseList(List<Resignation> resignations);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "approvedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "approvedByEmployee", ignore = true)
    ResignationDetailResponse toDetailResponse(Resignation resignation);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "decidedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "decidedByEmployee", ignore = true)
    TerminationResponse toResponse(Termination termination);

    List<TerminationResponse> toTerminationResponseList(List<Termination> terminations);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "decidedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "decidedByEmployee", ignore = true)
    TerminationDetailResponse toDetailResponse(Termination termination);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "interviewerEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "interviewerEmployee", ignore = true)
    ExitInterviewResponse toResponse(ExitInterview exitInterview);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "interviewerEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "interviewerEmployee", ignore = true)
    ExitInterviewDetailResponse toDetailResponse(ExitInterview exitInterview);

    @Mapping(target = "organizationalUnitName", ignore = true)
    @Mapping(target = "clearedByEmployeeName", ignore = true)
    @Mapping(target = "unreturnedAssetCount", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "clearedByEmployee", ignore = true)
    OffboardingClearanceResponse toResponse(OffboardingClearance clearance);

    List<OffboardingClearanceResponse> toClearanceResponseList(List<OffboardingClearance> clearances);
}

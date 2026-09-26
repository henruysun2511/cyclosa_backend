package com.cyclosa.onboarding.mapper;

import com.cyclosa.onboarding.dto.request.*;
import com.cyclosa.onboarding.dto.response.*;
import com.cyclosa.onboarding.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OnboardingMapper {

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    OnboardingChecklistTemplate toEntity(CreateChecklistTemplateRequest request);

    @Mapping(target = "template", ignore = true)
    OnboardingChecklistTemplateItem toEntity(CreateTemplateItemRequest request);

    @Mapping(target = "applicablePositionTitle", ignore = true)
    @Mapping(target = "applicableDepartmentName", ignore = true)
    @Mapping(target = "totalItems", ignore = true)
    ChecklistTemplateResponse toResponse(OnboardingChecklistTemplate template);

    List<ChecklistTemplateResponse> toTemplateResponseList(List<OnboardingChecklistTemplate> templates);

    @Mapping(target = "applicablePositionTitle", ignore = true)
    @Mapping(target = "applicableDepartmentName", ignore = true)
    ChecklistTemplateDetailResponse toDetailResponse(OnboardingChecklistTemplate template);

    @Mapping(target = "templateId", source = "template.id")
    @Mapping(target = "categoryDescription", expression = "java(item.getCategory() != null ? item.getCategory().getDescription() : null)")
    ChecklistTemplateItemResponse toItemResponse(OnboardingChecklistTemplateItem item);

    List<ChecklistTemplateItemResponse> toItemResponseList(List<OnboardingChecklistTemplateItem> items);

    @Mapping(target = "onboardingProcessId", source = "onboardingProcess.id")
    @Mapping(target = "categoryDescription", expression = "java(item.getCategory() != null ? item.getCategory().getDescription() : null)")
    @Mapping(target = "statusDescription", expression = "java(item.getStatus() != null ? item.getStatus().getDescription() : null)")
    @Mapping(target = "completedByEmployeeName", ignore = true)
    OnboardingProcessItemResponse toProcessItemResponse(OnboardingProcessItem item);

    List<OnboardingProcessItemResponse> toProcessItemResponseList(List<OnboardingProcessItem> items);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "positionTitle", ignore = true)
    @Mapping(target = "checklistTemplateName", ignore = true)
    @Mapping(target = "statusDescription", expression = "java(process.getStatus() != null ? process.getStatus().getDescription() : null)")
    @Mapping(target = "totalItems", ignore = true)
    @Mapping(target = "completedItems", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    OnboardingProcessResponse toProcessResponse(OnboardingProcess process);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "positionTitle", ignore = true)
    @Mapping(target = "checklistTemplateName", ignore = true)
    @Mapping(target = "statusDescription", expression = "java(process.getStatus() != null ? process.getStatus().getDescription() : null)")
    @Mapping(target = "progress", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "orientationSessions", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "allocatedAssets", ignore = true)
    OnboardingProcessDetailResponse toProcessDetailResponse(OnboardingProcess process);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "provisionedByEmployeeName", ignore = true)
    @Mapping(target = "statusDescription", expression = "java(account.getStatus() != null ? account.getStatus().getDescription() : null)")
    AccountProvisioningResponse toAccountResponse(AccountProvisioning account);

    List<AccountProvisioningResponse> toAccountResponseList(List<AccountProvisioning> accounts);

    @Mapping(target = "trainerEmployeeName", ignore = true)
    @Mapping(target = "statusDescription", expression = "java(session.getStatus() != null ? session.getStatus().getDescription() : null)")
    OrientationSessionResponse toSessionResponse(OrientationSession session);

    List<OrientationSessionResponse> toSessionResponseList(List<OrientationSession> sessions);

    @Mapping(target = "documentTypeDescription", expression = "java(document.getDocumentType() != null ? document.getDocumentType().getDescription() : null)")
    @Mapping(target = "verifiedByEmployeeName", ignore = true)
    EmployeeDocumentResponse toDocumentResponse(EmployeeDocument document);

    List<EmployeeDocumentResponse> toDocumentResponseList(List<EmployeeDocument> documents);
}

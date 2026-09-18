package com.cyclosa.workflow.dto.request;

import com.cyclosa.workflow.enums.ApprovalRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWorkflowDefinitionRequest {

    @NotNull(message = "Company ID không được để trống")
    private UUID companyId;

    @NotNull(message = "Loại yêu cầu không được để trống")
    private ApprovalRequestType requestType;

    @NotBlank(message = "Tên quy trình không được để trống")
    @Size(max = 150, message = "Tên quy trình tối đa 150 ký tự")
    private String name;

    private String description;
}

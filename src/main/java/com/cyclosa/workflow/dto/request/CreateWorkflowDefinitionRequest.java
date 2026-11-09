package com.cyclosa.workflow.dto.request;

import com.cyclosa.workflow.enums.RequestType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowDefinitionRequest {

    @NotNull(message = "Công ty áp dụng không được để trống")
    private UUID companyId;

    @NotNull(message = "Loại chứng từ áp dụng không được để trống")
    private RequestType requestType;

    @NotBlank(message = "Tên quy trình phê duyệt không được để trống")
    private String name;

    private String description;

    @NotEmpty(message = "Quy trình phải chứa ít nhất 01 bước duyệt")
    @Valid
    private List<CreateWorkflowStepRequest> steps;
}


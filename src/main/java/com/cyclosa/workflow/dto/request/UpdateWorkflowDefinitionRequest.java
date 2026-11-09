package com.cyclosa.workflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkflowDefinitionRequest {

    @NotBlank(message = "Tên quy trình phê duyệt không được để trống")
    private String name;

    private String description;

    @NotEmpty(message = "Quy trình phải chứa ít nhất 01 bước duyệt")
    @Valid
    private List<CreateWorkflowStepRequest> steps;
}


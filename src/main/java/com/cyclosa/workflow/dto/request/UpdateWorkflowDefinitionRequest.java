package com.cyclosa.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWorkflowDefinitionRequest {

    @NotBlank(message = "Tên quy trình không được để trống")
    @Size(max = 150, message = "Tên quy trình tối đa 150 ký tự")
    private String name;

    private String description;
}

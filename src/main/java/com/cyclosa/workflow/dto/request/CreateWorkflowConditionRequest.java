package com.cyclosa.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWorkflowConditionRequest {

    private UUID fromStepId;

    @NotNull(message = "Bước đích toStepId không được để trống")
    private UUID toStepId;

    @NotBlank(message = "Biểu thức điều kiện không được để trống")
    private String conditionExpression;

    @Builder.Default
    private Integer priority = 1;
}

package com.cyclosa.workflow.dto.request;

import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.StepConditionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowStepRequest {

    @NotNull(message = "Thứ tự bước không được để trống")
    @Min(value = 1, message = "Thứ tự bước phải từ 1 trở lên")
    private Integer stepOrder;

    @NotBlank(message = "Tên bước duyệt không được để trống")
    private String stepName;

    @NotNull(message = "Loại người duyệt không được để trống")
    private ApproverType approverType;

    private UUID approverRoleId;

    private UUID approverEmployeeId;

    @Builder.Default
    private StepConditionType conditionType = StepConditionType.ALWAYS;

    private BigDecimal conditionThreshold;
}


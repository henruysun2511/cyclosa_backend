package com.cyclosa.workflow.dto.request;

import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.OverdueAction;
import jakarta.validation.constraints.Min;
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
public class CreateWorkflowStepRequest {

    @NotNull(message = "Thứ tự bước không được để trống")
    @Min(value = 1, message = "Thứ tự bước phải lớn hơn hoặc bằng 1")
    private Integer stepOrder;

    @NotBlank(message = "Tên bước không được để trống")
    @Size(max = 150, message = "Tên bước tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Loại người duyệt không được để trống")
    private ApproverType approverType;

    private UUID specificApproverEmployeeId;

    private UUID specificRoleId;

    @Min(value = 1, message = "Thời hạn SLA tối thiểu là 1 giờ")
    private Integer slaHours;

    @Builder.Default
    private OverdueAction overdueAction = OverdueAction.REMIND;
}

package com.cyclosa.workflow.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWorkflowDelegateRequest {

    @NotNull(message = "Company ID không được để trống")
    private UUID companyId;

    @NotNull(message = "Người được ủy quyền không được để trống")
    private UUID delegateEmployeeId;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu ủy quyền không thể ở quá khứ")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String reason;
}

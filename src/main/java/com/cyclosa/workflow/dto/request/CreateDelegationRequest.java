package com.cyclosa.workflow.dto.request;

import com.cyclosa.workflow.enums.RequestType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDelegationRequest {

    @NotNull(message = "Công ty không được để trống")
    private UUID companyId;

    @NotNull(message = "Người nhận ủy quyền không được để trống")
    private UUID delegateeEmployeeId;

    private RequestType requestType; // null = ALL

    @NotNull(message = "Ngày bắt đầu ủy quyền không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc ủy quyền không được để trống")
    private LocalDate endDate;

    @NotBlank(message = "Lý do ủy quyền không được để trống")
    private String reason;
}


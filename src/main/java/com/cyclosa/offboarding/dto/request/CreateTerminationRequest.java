package com.cyclosa.offboarding.dto.request;

import com.cyclosa.offboarding.enums.TerminationReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTerminationRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    private UUID companyId;

    @NotNull(message = "Ngày làm việc cuối cùng không được để trống")
    private LocalDate lastWorkingDate;

    @NotNull(message = "Lý do chấm dứt không được để trống")
    private TerminationReason decisionReasonCategory;

    private UUID disciplineId;

    private String reasonDetail;
}

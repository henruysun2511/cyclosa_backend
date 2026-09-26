package com.cyclosa.offboarding.dto.request;

import com.cyclosa.offboarding.enums.ClearanceStatus;
import com.cyclosa.offboarding.enums.ClearanceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClearanceRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    @NotNull(message = "Loại thủ tục bàn giao không được để trống")
    private ClearanceType clearanceType;

    @NotNull(message = "Trạng thái bàn giao không được để trống")
    private ClearanceStatus status;

    private String note;
}

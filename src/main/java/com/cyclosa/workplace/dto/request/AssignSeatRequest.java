package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignSeatRequest {

    @NotNull(message = "Nhân viên (employeeId) không được để trống")
    private UUID employeeId;

    private String notes;
}

package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.enums.EmploymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChangeEmployeeStatusRequest {

    @NotNull(message = "Trạng thái nhân viên không được để trống")
    private EmploymentStatus status;

    private LocalDate effectiveDate;

    private String reason;
}

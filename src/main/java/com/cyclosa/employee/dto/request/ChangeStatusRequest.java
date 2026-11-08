package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private EmployeeStatus status;

    @NotNull(message = "Ngày có hiệu lực không được để trống")
    private LocalDate effectiveFrom;

    private String note;
}


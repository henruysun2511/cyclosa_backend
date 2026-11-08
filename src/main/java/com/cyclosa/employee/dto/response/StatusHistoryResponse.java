package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.entity.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryResponse {

    private UUID id;
    private UUID employeeId;
    private EmployeeStatus status;
    private LocalDate effectiveFrom;
    private String note;
    private LocalDateTime createdAt;
}


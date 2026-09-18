package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.enums.EmployeeChangeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class EmployeeHistoryResponse {

    private UUID id;
    private UUID employeeId;
    private EmployeeChangeType changeType;
    private String oldValue;
    private String newValue;
    private LocalDate effectiveDate;
    private UUID changedByEmployeeId;
    private String changedByFullName;
    private String reason;
    private LocalDateTime createdAt;
}

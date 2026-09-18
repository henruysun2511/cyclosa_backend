package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.enums.EmploymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class EmployeeFilter {

    private String keyword;
    private UUID companyId;
    private UUID organizationalUnitId;
    private UUID branchId;
    private UUID positionId;
    private EmploymentStatus status;
    private LocalDate hireDateFrom;
    private LocalDate hireDateTo;

    // --- Các trường phục vụ DataScope Filtering nội bộ ---
    private UUID exactEmployeeId;
    private UUID directManagerId;
    private Set<UUID> allowedUnitIds;
}

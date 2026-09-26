package com.cyclosa.offboarding.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.offboarding.enums.ClearanceStatus;
import com.cyclosa.offboarding.enums.ClearanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffboardingClearanceResponse {

    private UUID id;

    private UUID employeeId;

    private EmployeeSummary employee;

    private UUID companyId;

    private UUID organizationalUnitId;

    private String organizationalUnitName;

    private ClearanceType clearanceType;

    private UUID clearedByEmployeeId;

    private String clearedByEmployeeName;

    private EmployeeSummary clearedByEmployee;

    private ClearanceStatus status;

    private String note;

    private LocalDateTime clearedAt;

    private Integer unreturnedAssetCount;
}

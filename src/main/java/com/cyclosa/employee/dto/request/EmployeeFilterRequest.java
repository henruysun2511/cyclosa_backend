package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFilterRequest {

    private UUID companyId;
    private String keyword;
    private UUID departmentId;
    private UUID branchId;
    private UUID jobTitleId;
    private EmployeeStatus status;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    private String sort;
}


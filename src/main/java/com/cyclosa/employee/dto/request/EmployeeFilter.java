package com.cyclosa.employee.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.employee.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang hồ sơ nhân sự")
public class EmployeeFilter extends BaseFilterRequest {

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

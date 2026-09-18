package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.enums.EmploymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UpdateEmploymentInfoRequest {

    @NotNull(message = "Phòng ban / đơn vị tổ chức không được để trống")
    private UUID organizationalUnitId;

    @NotNull(message = "Chi nhánh làm việc không được để trống")
    private UUID branchId;

    @NotNull(message = "Chức danh / vị trí việc làm không được để trống")
    private UUID positionId;

    private UUID jobLevelId;
    private UUID managerEmployeeId;
    private EmploymentType employmentType;
    private String companyEmail;
    private String workLocation;
    private LocalDate probationEndDate;

    /**
     * Lý do điều chuyển / bổ nhiệm để lưu vết vào lịch sử công tác
     */
    private String changeReason;
}

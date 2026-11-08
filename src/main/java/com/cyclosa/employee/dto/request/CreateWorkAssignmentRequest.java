package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.AssignmentChangeReason;
import com.cyclosa.employee.entity.enums.EmploymentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkAssignmentRequest {

    @NotNull(message = "Ngày có hiệu lực không được để trống")
    private LocalDate effectiveFrom;

    @NotNull(message = "Phòng ban trực thuộc không được để trống")
    private UUID departmentId;

    @NotNull(message = "Chi nhánh làm việc không được để trống")
    private UUID branchId;

    @NotNull(message = "Chức danh công việc không được để trống")
    private UUID jobTitleId;

    @NotNull(message = "Cấp bậc chức danh không được để trống")
    private UUID jobLevelId;

    private UUID managerEmployeeId;

    @Builder.Default
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Builder.Default
    private AssignmentChangeReason changeReason = AssignmentChangeReason.PROMOTION;

    private UUID transferRequestId;
}


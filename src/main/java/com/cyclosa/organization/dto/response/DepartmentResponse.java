package com.cyclosa.organization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phòng ban / đơn vị")
public class DepartmentResponse {

    @Schema(description = "ID phòng ban", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "ID công ty trực thuộc")
    private UUID companyId;

    @Schema(description = "Mã phòng ban", example = "DEPT_TECH_01")
    private String code;

    @Schema(description = "Tên phòng ban hiện tại", example = "Phòng Phát triển Phần mềm")
    private String name;

    @Schema(description = "ID phòng ban cha hiện tại")
    private UUID parentDepartmentId;

    @Schema(description = "Tên phòng ban cha")
    private String parentDepartmentName;

    @Schema(description = "ID trung tâm chi phí hiện tại")
    private UUID costCenterId;

    @Schema(description = "Tên trung tâm chi phí hiện tại")
    private String costCenterName;

    @Schema(description = "ID trưởng phòng hiện tại")
    private UUID managerEmployeeId;

    @Schema(description = "Kế thừa quyền xuống cấp dưới")
    private Boolean isPermissionInherited;

    @Schema(description = "Ngày hiệu lực của phiên bản áp dụng")
    private LocalDate effectiveFrom;

    @Schema(description = "Ngày giải thể (nếu có)")
    private LocalDate dissolvedDate;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    @Schema(description = "Thời gian tạo bản ghi")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}


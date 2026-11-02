package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin phòng ban (tạo phiên bản hiệu lực mới)")
public class UpdateDepartmentRequest {

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 150, message = "Tên phòng ban tối đa 150 ký tự")
    @Schema(description = "Tên phòng ban", example = "Phòng Kỹ thuật & Công nghệ số")
    private String name;

    @Schema(description = "ID phòng ban cha", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID parentDepartmentId;

    @Schema(description = "ID trung tâm chi phí hạch toán", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID costCenterId;

    @Schema(description = "ID nhân viên trưởng bộ phận", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID managerEmployeeId;

    @NotNull(message = "Ngày hiệu lực không được để trống")
    @Schema(description = "Ngày bắt đầu áp dụng cơ cấu phiên bản mới", example = "2026-06-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Cho phép các đơn vị cấp dưới kế thừa quyền từ phòng ban này", example = "true")
    private Boolean isPermissionInherited;
}


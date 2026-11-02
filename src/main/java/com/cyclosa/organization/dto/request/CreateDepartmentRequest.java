package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới phòng ban / đơn vị")
public class CreateDepartmentRequest {

    @NotNull(message = "ID công ty không được để trống")
    @Schema(description = "ID công ty trực thuộc", example = "00000000-0000-0000-0000-000000000001")
    private UUID companyId;

    @NotBlank(message = "Mã phòng ban không được để trống")
    @Size(max = 50, message = "Mã phòng ban tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã chỉ chứa chữ hoa, số, gạch dưới và gạch ngang")
    @Schema(description = "Mã phòng ban", example = "DEPT_TECH_01")
    private String code;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 150, message = "Tên phòng ban tối đa 150 ký tự")
    @Schema(description = "Tên phòng ban", example = "Phòng Phát triển Phần mềm")
    private String name;

    @Schema(description = "ID phòng ban cha (để trống nếu là gốc)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID parentDepartmentId;

    @Schema(description = "ID trung tâm chi phí hạch toán", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID costCenterId;

    @Schema(description = "ID nhân viên trưởng bộ phận", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID managerEmployeeId;

    @NotNull(message = "Ngày hiệu lực không được để trống")
    @Schema(description = "Ngày bắt đầu áp dụng cơ cấu phiên bản", example = "2026-01-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Cho phép các đơn vị cấp dưới kế thừa quyền từ phòng ban này", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean isPermissionInherited = true;
}


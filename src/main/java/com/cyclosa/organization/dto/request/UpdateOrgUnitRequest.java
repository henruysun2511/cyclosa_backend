package com.cyclosa.organization.dto.request;

import com.cyclosa.common.enums.ActiveStatus;
import com.cyclosa.organization.enums.UnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu cập nhật thông tin đơn vị tổ chức")
public class UpdateOrgUnitRequest {

    @NotBlank(message = "Tên đơn vị không được để trống")
    @Size(max = 150, message = "Tên đơn vị không vượt quá 150 ký tự")
    @Schema(description = "Tên đơn vị", example = "Phòng Công nghệ & Chuyển đổi số")
    private String name;

    @NotNull(message = "Loại đơn vị không được để trống")
    @Schema(description = "Loại cấp bậc đơn vị (DIVISION, DEPARTMENT, TEAM, SECTION)", example = "DEPARTMENT")
    private UnitType unitType;

    @Schema(description = "ID trung tâm chi phí gắn kèm", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID costCenterId;

    @Schema(description = "ID nhân viên giữ vị trí người đứng đầu/quản lý đơn vị", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID managerEmployeeId;

    @Schema(description = "Mô tả chức năng nhiệm vụ của phòng ban", example = "Phụ trách phát triển và vận hành hệ thống phần mềm")
    private String description;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Schema(description = "Trạng thái hoạt động", example = "ACTIVE")
    private ActiveStatus status;
}

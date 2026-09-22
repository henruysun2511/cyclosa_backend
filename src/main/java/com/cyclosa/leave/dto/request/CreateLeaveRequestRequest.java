package com.cyclosa.leave.dto.request;

import com.cyclosa.leave.enums.LeaveSession;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Yêu cầu tạo đơn xin nghỉ phép")
public class CreateLeaveRequestRequest {

    @Schema(description = "ID nhân viên (nếu để trống, hệ thống tự lấy nhân viên từ tài khoản đang đăng nhập)")
    private UUID employeeId;

    @NotNull(message = "Loại nghỉ phép không được để trống")
    @Schema(description = "ID loại ngày nghỉ phép")
    private UUID leaveTypeId;

    @NotNull(message = "Ngày bắt đầu nghỉ không được để trống")
    @Schema(description = "Ngày bắt đầu nghỉ", example = "2026-10-01")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc nghỉ không được để trống")
    @Schema(description = "Ngày kết thúc nghỉ", example = "2026-10-02")
    private LocalDate endDate;

    @Schema(description = "Buổi nghỉ (FULL_DAY, MORNING, AFTERNOON). Mặc định là FULL_DAY", example = "FULL_DAY")
    @Builder.Default
    private LeaveSession session = LeaveSession.FULL_DAY;

    @NotBlank(message = "Lý do xin nghỉ không được để trống")
    @Schema(description = "Lý do xin nghỉ phép", example = "Nghỉ phép giải quyết việc gia đình")
    private String reason;
}

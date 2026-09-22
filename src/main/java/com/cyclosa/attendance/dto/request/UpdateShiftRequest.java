package com.cyclosa.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật ca làm việc")
public class UpdateShiftRequest {

    @NotBlank(message = "Tên ca làm việc không được để trống")
    @Size(max = 150, message = "Tên ca làm việc tối đa 150 ký tự")
    @Schema(description = "Tên ca làm việc", example = "Ca Hành chính Văn phòng")
    private String name;

    @NotNull(message = "Giờ bắt đầu ca không được để trống")
    @Schema(description = "Giờ bắt đầu ca", example = "08:00:00")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc ca không được để trống")
    @Schema(description = "Giờ kết thúc ca", example = "17:30:00")
    private LocalTime endTime;

    @Schema(description = "Giờ bắt đầu nghỉ ca", example = "12:00:00")
    private LocalTime breakStartTime;

    @Schema(description = "Giờ kết thúc nghỉ ca", example = "13:30:00")
    private LocalTime breakEndTime;

    @Schema(description = "Tổng giờ làm việc thực tế quy định", example = "8.00")
    private BigDecimal workingHours;

    @Schema(description = "Số công quy đổi nếu làm đủ ca", example = "1.00")
    private BigDecimal workUnits;

    @Schema(description = "Số phút ân hạn đi muộn không phạt", example = "15")
    private Integer graceLateMinutes;

    @Schema(description = "Số phút ân hạn về sớm không phạt", example = "0")
    private Integer graceEarlyMinutes;

    @Schema(description = "Đánh dấu ca đêm", example = "false")
    private Boolean isNightShift;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}

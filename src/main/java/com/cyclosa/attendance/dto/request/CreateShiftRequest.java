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
@Schema(description = "Yêu cầu tạo mới ca làm việc")
public class CreateShiftRequest {

    @NotBlank(message = "Mã ca làm việc không được để trống")
    @Size(max = 50, message = "Mã ca làm việc tối đa 50 ký tự")
    @Schema(description = "Mã ca làm việc", example = "CA_HANH_CHINH")
    private String code;

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
    @Builder.Default
    private BigDecimal workingHours = BigDecimal.valueOf(8.00);

    @Schema(description = "Số công quy đổi nếu làm đủ ca", example = "1.00")
    @Builder.Default
    private BigDecimal workUnits = BigDecimal.valueOf(1.00);

    @Schema(description = "Số phút ân hạn đi muộn không phạt", example = "15")
    @Builder.Default
    private Integer graceLateMinutes = 0;

    @Schema(description = "Số phút ân hạn về sớm không phạt", example = "0")
    @Builder.Default
    private Integer graceEarlyMinutes = 0;

    @Schema(description = "Đánh dấu ca đêm", example = "false")
    @Builder.Default
    private Boolean isNightShift = false;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    @Builder.Default
    private Boolean isActive = true;
}

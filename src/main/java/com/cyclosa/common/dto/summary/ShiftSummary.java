package com.cyclosa.common.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt ca làm việc")
public class ShiftSummary {

    @Schema(description = "ID ca làm việc")
    private UUID id;

    @Schema(description = "Mã ca làm việc", example = "HC_01")
    private String code;

    @Schema(description = "Tên ca làm việc", example = "Ca hành chính")
    private String name;

    @Schema(description = "Giờ bắt đầu", example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "Giờ kết thúc", example = "17:00:00")
    private LocalTime endTime;

    @Schema(description = "Thời gian nghỉ giữa ca (phút)", example = "60")
    private Integer breakMinutes;
}

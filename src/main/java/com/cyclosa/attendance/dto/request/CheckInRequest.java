package com.cyclosa.attendance.dto.request;

import com.cyclosa.attendance.enums.CheckMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu thực hiện Check-in vào ca")
public class CheckInRequest {

    @Schema(description = "Vĩ độ GPS", example = "21.028511")
    private Double latitude;

    @Schema(description = "Kinh độ GPS", example = "105.854444")
    private Double longitude;

    @NotNull(message = "Phương thức chấm công không được để trống")
    @Schema(description = "Phương thức chấm công", example = "GPS")
    @Builder.Default
    private CheckMethod method = CheckMethod.GPS;

    @Schema(description = "ID ca làm việc (để trống nếu tự động theo lịch phân ca)")
    private UUID shiftId;

    @Schema(description = "Ghi chú chấm công", example = "Check-in tại sảnh văn phòng")
    private String note;
}

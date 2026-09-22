package com.cyclosa.attendance.dto.request;

import com.cyclosa.attendance.enums.ExplanationReasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo đơn giải trình chấm công")
public class CreateExplanationRequest {

    @Schema(description = "ID bản ghi chấm công cần giải trình (nếu có)")
    private UUID attendanceRecordId;

    @NotNull(message = "Ngày cần giải trình không được để trống")
    @Schema(description = "Ngày cần giải trình", example = "2026-10-15")
    private LocalDate workDate;

    @NotNull(message = "Loại lý do giải trình không được để trống")
    @Schema(description = "Loại lý do giải trình", example = "FORGOT_CHECK_IN")
    private ExplanationReasonType reasonType;

    @Schema(description = "Giờ đề xuất Check-in", example = "08:00:00")
    private LocalTime proposedCheckIn;

    @Schema(description = "Giờ đề xuất Check-out", example = "17:30:00")
    private LocalTime proposedCheckOut;

    @NotBlank(message = "Lý do giải trình không được để trống")
    @Schema(description = "Lý do giải trình chi tiết", example = "Quên bấm máy chấm công do vội họp với khách hàng lúc đầu giờ sáng")
    private String reason;

    @Schema(description = "Đường dẫn ảnh/tài liệu minh chứng đính kèm", example = "https://res.cloudinary.com/cyclosa/image/proof123.jpg")
    private String proofUrl;
}

package com.cyclosa.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu lên lịch buổi đào tạo hội nhập / định hướng")
public class CreateOrientationSessionRequest {

    @NotBlank(message = "Tên buổi đào tạo định hướng không được để trống")
    @Size(max = 200, message = "Tên buổi định hướng tối đa 200 ký tự")
    @Schema(description = "Tên buổi định hướng", example = "Chào đón nhân viên mới & Văn hóa doanh nghiệp")
    private String sessionName;

    @Schema(description = "Nội dung tóm tắt buổi đào tạo")
    private String description;

    @Schema(description = "Địa điểm hoặc link Google Meet / MS Teams", example = "Phòng họp Lớn Tầng 3 hoặc https://meet.google.com/xyz")
    private String location;

    @NotNull(message = "Thời gian đào tạo không được để trống")
    @Schema(description = "Thời gian tổ chức")
    private LocalDateTime scheduledAt;

    @Schema(description = "ID nhân viên đào tạo / diễn giả (Trainer)")
    private UUID trainerEmployeeId;
}

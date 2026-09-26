package com.cyclosa.onboarding.dto.request;

import com.cyclosa.onboarding.enums.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu cập nhật buổi đào tạo hội nhập")
public class UpdateOrientationSessionRequest {

    @Size(max = 200, message = "Tên buổi định hướng tối đa 200 ký tự")
    @Schema(description = "Tên buổi định hướng")
    private String sessionName;

    @Schema(description = "Nội dung tóm tắt buổi đào tạo")
    private String description;

    @Schema(description = "Địa điểm hoặc link online")
    private String location;

    @Schema(description = "Thời gian tổ chức")
    private LocalDateTime scheduledAt;

    @Schema(description = "ID nhân viên đào tạo / diễn giả (Trainer)")
    private UUID trainerEmployeeId;

    @Schema(description = "Trạng thái buổi đào tạo: SCHEDULED, COMPLETED, CANCELLED")
    private SessionStatus status;
}

package com.cyclosa.onboarding.dto.response;

import com.cyclosa.onboarding.enums.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin buổi đào tạo hội nhập / định hướng")
public class OrientationSessionResponse {

    private UUID id;
    private UUID companyId;
    private UUID onboardingProcessId;
    private String sessionName;
    private String description;
    private String location;
    private LocalDateTime scheduledAt;
    private UUID trainerEmployeeId;
    private String trainerEmployeeName;
    private SessionStatus status;
    private String statusDescription;
}

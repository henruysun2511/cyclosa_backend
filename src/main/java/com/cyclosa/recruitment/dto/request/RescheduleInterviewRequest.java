package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleInterviewRequest {

    @NotNull(message = "Thời gian bắt đầu mới không được để trống")
    private LocalDateTime newStartTime;

    @NotNull(message = "Thời gian kết thúc mới không được để trống")
    private LocalDateTime newEndTime;

    private String reason;

    private String locationOrMeetingUrl;
}

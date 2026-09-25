package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.InterviewType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleInterviewRequest {

    private UUID companyId;

    @NotNull(message = "Hồ sơ ứng tuyển không được để trống")
    private UUID applicationId;

    private UUID interviewKitId;

    @NotNull(message = "Loại vòng phỏng vấn không được để trống")
    private InterviewType interviewType;

    @Builder.Default
    private Integer roundNumber = 1;

    @NotNull(message = "Thời gian bắt đầu phỏng vấn không được để trống")
    private LocalDateTime scheduledStartTime;

    @NotNull(message = "Thời gian kết thúc phỏng vấn không được để trống")
    private LocalDateTime scheduledEndTime;

    private String locationOrMeetingUrl;

    private String notes;

    private List<UUID> panelInterviewerEmployeeIds;
}

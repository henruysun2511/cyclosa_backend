package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.InterviewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInterviewKitRequest {

    private UUID companyId;

    @NotNull(message = "Vị trí tuyển dụng không được để trống")
    private UUID jobPositionId;

    @NotNull(message = "Loại vòng phỏng vấn không được để trống")
    private InterviewType interviewType;

    @NotBlank(message = "Tiêu đề bộ tiêu chí không được để trống")
    private String title;

    private String description;

    private List<CreateInterviewQuestionRequest> questions;
}

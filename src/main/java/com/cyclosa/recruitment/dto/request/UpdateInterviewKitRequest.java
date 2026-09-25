package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.InterviewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInterviewKitRequest {

    @NotNull(message = "Loại vòng phỏng vấn không được để trống")
    private InterviewType interviewType;

    @NotBlank(message = "Tiêu đề bộ tiêu chí không được để trống")
    private String title;

    private String description;
}

package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInterviewQuestionRequest {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String questionText;

    @NotBlank(message = "Tiêu chí chấm điểm không được để trống")
    private String criteria;

    @Builder.Default
    private Integer maxScore = 5;

    @Builder.Default
    private BigDecimal weight = BigDecimal.ONE;

    @Builder.Default
    private Integer sortOrder = 0;
}

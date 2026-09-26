package com.cyclosa.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật lộ trình thăng tiến chuẩn")
public class UpdateCareerPathRequest {

    @Schema(description = "Mô tả tiêu chuẩn, điều kiện thăng tiến")
    private String description;

    @DecimalMin(value = "0.0", message = "Số năm kinh nghiệm yêu cầu tối thiểu phải >= 0")
    @Schema(description = "Số năm kinh nghiệm tối thiểu yêu cầu")
    private BigDecimal minYearsRequired;
}

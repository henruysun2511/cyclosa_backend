package com.cyclosa.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo lộ trình thăng tiến chuẩn")
public class CreateCareerPathRequest {

    @NotNull(message = "Vị trí xuất phát không được để trống")
    @Schema(description = "ID vị trí xuất phát")
    private UUID fromPositionId;

    @NotNull(message = "Vị trí đích không được để trống")
    @Schema(description = "ID vị trí đích")
    private UUID toPositionId;

    @Schema(description = "Mô tả tiêu chuẩn, điều kiện thăng tiến")
    private String description;

    @DecimalMin(value = "0.0", message = "Số năm kinh nghiệm yêu cầu tối thiểu phải >= 0")
    @Schema(description = "Số năm kinh nghiệm tối thiểu yêu cầu (VD: 2.0 năm)")
    private BigDecimal minYearsRequired;
}

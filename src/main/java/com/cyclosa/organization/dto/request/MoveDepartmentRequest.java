package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu điều chuyển vị trí phòng ban trong cây cơ cấu")
public class MoveDepartmentRequest {

    @Schema(description = "ID phòng ban cha mới (để null nếu đưa lên làm phòng ban cấp cao nhất)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID newParentDepartmentId;

    @NotNull(message = "Ngày hiệu lực không được để trống")
    @Schema(description = "Ngày bắt đầu áp dụng vị trí cơ cấu mới", example = "2026-07-01")
    private LocalDate effectiveFrom;
}


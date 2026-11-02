package com.cyclosa.organization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết cấp bậc chức danh")
public class JobLevelResponse {

    @Schema(description = "ID cấp bậc", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "ID công ty trực thuộc")
    private UUID companyId;

    @Schema(description = "Mã cấp bậc", example = "LVL_03")
    private String code;

    @Schema(description = "Tên cấp bậc", example = "Level 3 - Senior")
    private String name;

    @Schema(description = "Thứ tự phân cấp", example = "3")
    private Integer rankOrder;

    @Schema(description = "Thời gian tạo bản ghi")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}


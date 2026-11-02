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
@Schema(description = "Thông tin chi tiết khu vực / vùng miền")
public class RegionResponse {

    @Schema(description = "ID khu vực", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "ID công ty trực thuộc")
    private UUID companyId;

    @Schema(description = "Mã khu vực", example = "REG_NORTH")
    private String code;

    @Schema(description = "Tên khu vực", example = "Khu vực Miền Bắc")
    private String name;

    @Schema(description = "Số lượng chi nhánh trực thuộc")
    private Integer branchCount;

    @Schema(description = "Thời gian tạo bản ghi")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}


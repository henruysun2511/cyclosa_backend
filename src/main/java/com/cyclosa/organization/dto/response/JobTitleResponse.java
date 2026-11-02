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
@Schema(description = "Thông tin chi tiết chức danh công việc")
public class JobTitleResponse {

    @Schema(description = "ID chức danh", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "ID công ty trực thuộc")
    private UUID companyId;

    @Schema(description = "Mã chức danh", example = "DEV_SR")
    private String code;

    @Schema(description = "Tên chức danh", example = "Senior Software Engineer")
    private String name;

    @Schema(description = "Mô tả tiêu chuẩn công việc")
    private String description;

    @Schema(description = "Thời gian tạo bản ghi")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}


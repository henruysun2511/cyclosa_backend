package com.cyclosa.organization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết chi nhánh / văn phòng")
public class BranchResponse {

    @Schema(description = "ID chi nhánh", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "ID khu vực trực thuộc")
    private UUID regionId;

    @Schema(description = "Tên khu vực trực thuộc")
    private String regionName;

    @Schema(description = "Mã chi nhánh", example = "BR_HN_01")
    private String code;

    @Schema(description = "Tên chi nhánh", example = "Chi nhánh Hà Nội")
    private String name;

    @Schema(description = "Địa chỉ chi nhánh")
    private String address;

    @Schema(description = "Tọa độ vĩ độ (Latitude)")
    private BigDecimal latitude;

    @Schema(description = "Tọa độ kinh độ (Longitude)")
    private BigDecimal longitude;

    @Schema(description = "Bán kính cho phép chấm công GPS (mét)")
    private Integer attendanceRadiusMeters;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean isActive;

    @Schema(description = "Thời gian tạo bản ghi")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}


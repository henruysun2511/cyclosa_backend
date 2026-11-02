package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin chi nhánh / văn phòng")
public class UpdateBranchRequest {

    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 150, message = "Tên chi nhánh tối đa 150 ký tự")
    @Schema(description = "Tên chi nhánh", example = "Chi nhánh Hà Nội")
    private String name;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    @Schema(description = "Địa chỉ chi nhánh", example = "Số 1 Thái Hà, Đống Đa, Hà Nội")
    private String address;

    @DecimalMin(value = "-90.0", message = "Vĩ độ không hợp lệ (-90 đến 90)")
    @DecimalMax(value = "90.0", message = "Vĩ độ không hợp lệ (-90 đến 90)")
    @Schema(description = "Tọa độ vĩ độ (Latitude) cho GPS chấm công", example = "21.0123456")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Kinh độ không hợp lệ (-180 đến 180)")
    @DecimalMax(value = "180.0", message = "Kinh độ không hợp lệ (-180 đến 180)")
    @Schema(description = "Tọa độ kinh độ (Longitude) cho GPS chấm công", example = "105.8123456")
    private BigDecimal longitude;

    @Min(value = 10, message = "Bán kính chấm công tối thiểu 10m")
    @Max(value = 10000, message = "Bán kính chấm công tối đa 10,000m")
    @Schema(description = "Bán kính cho phép chấm công GPS (mét)", example = "150")
    private Integer attendanceRadiusMeters;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}


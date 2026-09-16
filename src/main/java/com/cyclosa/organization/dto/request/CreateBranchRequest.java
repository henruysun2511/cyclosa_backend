package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu tạo mới chi nhánh làm việc")
public class CreateBranchRequest {

    @Schema(description = "ID công ty sở hữu", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;

    @NotNull(message = "Vùng miền không được để trống")
    @Schema(description = "ID vùng miền trực thuộc", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID regionId;

    @NotBlank(message = "Mã chi nhánh không được để trống")
    @Size(max = 50, message = "Mã chi nhánh không vượt quá 50 ký tự")
    @Schema(description = "Mã chi nhánh", example = "HN_HQ")
    private String code;

    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 150, message = "Tên chi nhánh không vượt quá 150 ký tự")
    @Schema(description = "Tên chi nhánh", example = "Trụ sở chính Hà Nội")
    private String name;

    @Schema(description = "Địa chỉ chi tiết", example = "Tầng 12, Keangnam Landmark 72, Nam Từ Liêm, Hà Nội")
    private String address;

    @Schema(description = "Tọa độ vĩ độ (Latitude) phục vụ chấm công", example = "21.0168450")
    private BigDecimal latitude;

    @Schema(description = "Tọa độ kinh độ (Longitude) phục vụ chấm công", example = "105.7838520")
    private BigDecimal longitude;

    @Schema(description = "Bán kính cho phép điểm danh chấm công (mét)", example = "200")
    private Integer checkinRadiusMeters;
}

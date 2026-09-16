package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu tạo mới trung tâm chi phí (Cost Center)")
public class CreateCostCenterRequest {

    @Schema(description = "ID công ty sở hữu", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;

    @NotBlank(message = "Mã trung tâm chi phí không được để trống")
    @Size(max = 50, message = "Mã trung tâm chi phí không vượt quá 50 ký tự")
    @Schema(description = "Mã định danh hạch toán chi phí", example = "CC_TECH_01")
    private String code;

    @NotBlank(message = "Tên trung tâm chi phí không được để trống")
    @Size(max = 150, message = "Tên trung tâm chi phí không vượt quá 150 ký tự")
    @Schema(description = "Tên trung tâm chi phí", example = "Trung tâm Chi phí Nghiên cứu & Phát triển")
    private String name;

    @Schema(description = "Mô tả mục đích sử dụng chi phí", example = "Hạch toán toàn bộ chi phí lương và công cụ của khối Tech")
    private String description;
}

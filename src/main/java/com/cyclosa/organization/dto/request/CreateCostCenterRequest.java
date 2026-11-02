package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới trung tâm chi phí")
public class CreateCostCenterRequest {

    @NotNull(message = "ID công ty không được để trống")
    @Schema(description = "ID công ty trực thuộc", example = "00000000-0000-0000-0000-000000000001")
    private UUID companyId;

    @NotBlank(message = "Mã trung tâm chi phí không được để trống")
    @Size(max = 50, message = "Mã trung tâm chi phí tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã chỉ chứa chữ hoa, số, gạch dưới và gạch ngang")
    @Schema(description = "Mã trung tâm chi phí", example = "CC_IT_01")
    private String code;

    @NotBlank(message = "Tên trung tâm chi phí không được để trống")
    @Size(max = 150, message = "Tên tối đa 150 ký tự")
    @Schema(description = "Tên trung tâm chi phí", example = "Khối Công nghệ & Phần mềm")
    private String name;

    @Schema(description = "Mô tả chi tiết mục đích hạch toán ngân sách")
    private String description;
}


package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin trung tâm chi phí")
public class UpdateCostCenterRequest {

    @NotBlank(message = "Tên trung tâm chi phí không được để trống")
    @Size(max = 150, message = "Tên tối đa 150 ký tự")
    @Schema(description = "Tên trung tâm chi phí", example = "Khối Công nghệ & Phần mềm")
    private String name;

    @Schema(description = "Mô tả chi tiết mục đích hạch toán ngân sách")
    private String description;
}


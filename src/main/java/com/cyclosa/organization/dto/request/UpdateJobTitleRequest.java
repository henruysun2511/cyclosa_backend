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
@Schema(description = "Yêu cầu cập nhật thông tin chức danh công việc")
public class UpdateJobTitleRequest {

    @NotBlank(message = "Tên chức danh không được để trống")
    @Size(max = 150, message = "Tên chức danh tối đa 150 ký tự")
    @Schema(description = "Tên chức danh", example = "Senior Software Engineer")
    private String name;

    @Schema(description = "Mô tả tiêu chuẩn và nhiệm vụ công việc")
    private String description;
}


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
@Schema(description = "Yêu cầu tạo mới chức danh công việc")
public class CreateJobTitleRequest {

    @NotNull(message = "ID công ty không được để trống")
    @Schema(description = "ID công ty trực thuộc", example = "00000000-0000-0000-0000-000000000001")
    private UUID companyId;

    @NotBlank(message = "Mã chức danh không được để trống")
    @Size(max = 50, message = "Mã chức danh tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã chỉ chứa chữ hoa, số, gạch dưới và gạch ngang")
    @Schema(description = "Mã chức danh công việc", example = "DEV_SR")
    private String code;

    @NotBlank(message = "Tên chức danh không được để trống")
    @Size(max = 150, message = "Tên chức danh tối đa 150 ký tự")
    @Schema(description = "Tên chức danh", example = "Senior Software Engineer")
    private String name;

    @Schema(description = "Mô tả tiêu chuẩn và nhiệm vụ công việc")
    private String description;
}


package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin cấp bậc chức danh")
public class UpdateJobLevelRequest {

    @NotBlank(message = "Tên cấp bậc không được để trống")
    @Size(max = 100, message = "Tên cấp bậc tối đa 100 ký tự")
    @Schema(description = "Tên cấp bậc", example = "Level 3 - Senior")
    private String name;

    @NotNull(message = "Thứ tự cấp bậc không được để trống")
    @Schema(description = "Thứ tự cấp bậc (1 là thấp nhất, số càng lớn cấp càng cao)", example = "3")
    private Integer rankOrder;
}


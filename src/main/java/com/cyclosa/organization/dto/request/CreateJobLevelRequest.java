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
@Schema(description = "Yêu cầu tạo mới cấp bậc chức danh")
public class CreateJobLevelRequest {

    @NotNull(message = "ID công ty không được để trống")
    @Schema(description = "ID công ty trực thuộc", example = "00000000-0000-0000-0000-000000000001")
    private UUID companyId;

    @NotBlank(message = "Mã cấp bậc không được để trống")
    @Size(max = 50, message = "Mã cấp bậc tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã chỉ chứa chữ hoa, số, gạch dưới và gạch ngang")
    @Schema(description = "Mã cấp bậc", example = "LVL_03")
    private String code;

    @NotBlank(message = "Tên cấp bậc không được để trống")
    @Size(max = 100, message = "Tên cấp bậc tối đa 100 ký tự")
    @Schema(description = "Tên cấp bậc", example = "Level 3 - Senior / Chuyên viên cao cấp")
    private String name;

    @NotNull(message = "Thứ tự cấp bậc không được để trống")
    @Schema(description = "Thứ tự cấp bậc (1 là thấp nhất, số càng lớn cấp càng cao)", example = "3")
    private Integer rankOrder;
}


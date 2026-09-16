package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu tạo mới cấp bậc công việc")
public class CreateJobLevelRequest {

    @Schema(description = "ID công ty sở hữu", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;

    @NotBlank(message = "Mã cấp bậc không được để trống")
    @Size(max = 50, message = "Mã cấp bậc không vượt quá 50 ký tự")
    @Schema(description = "Mã cấp bậc", example = "LEVEL_3")
    private String code;

    @NotBlank(message = "Tên cấp bậc không được để trống")
    @Size(max = 100, message = "Tên cấp bậc không vượt quá 100 ký tự")
    @Schema(description = "Tên cấp bậc", example = "Chuyên viên (Senior/Specialist)")
    private String name;

    @NotNull(message = "Thứ tự cấp bậc không được để trống")
    @Min(value = 1, message = "Thứ tự cấp bậc phải lớn hơn hoặc bằng 1")
    @Schema(description = "Thứ tự cấp bậc theo thang từ thấp đến cao", example = "3")
    private Integer rankOrder;
}

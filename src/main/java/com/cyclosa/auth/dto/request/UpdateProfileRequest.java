package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin cá nhân")
public class UpdateProfileRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Tên đăng nhập chỉ chứa chữ cái, chữ số, dấu chấm, dấu gạch ngang và dấu gạch dưới")
    @Schema(description = "Tên đăng nhập", example = "an.nguyen")
    private String username;
}

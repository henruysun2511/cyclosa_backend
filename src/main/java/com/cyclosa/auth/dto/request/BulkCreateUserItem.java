package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin một tài khoản cần tạo trong danh sách nhập hàng loạt")
public class BulkCreateUserItem {

    @Schema(description = "Tên đăng nhập (nếu để trống sẽ tự sinh từ email)", example = "an.nguyen")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Địa chỉ email của nhân viên", example = "an.nguyen@cyclosa.vn")
    private String email;
}

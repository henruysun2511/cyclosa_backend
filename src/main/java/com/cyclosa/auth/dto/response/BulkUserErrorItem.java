package com.cyclosa.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết tài khoản không thể tạo trong quá trình nhập hàng loạt")
public class BulkUserErrorItem {

    @Schema(description = "Địa chỉ email bị lỗi", example = "duplicate@cyclosa.vn")
    private String email;

    @Schema(description = "Tên đăng nhập", example = "duplicate_user")
    private String username;

    @Schema(description = "Lý do không thể tạo", example = "Email đã tồn tại trong hệ thống")
    private String reason;
}

package com.cyclosa.auth.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái tài khoản người dùng")
public enum UserStatus {
    @Schema(description = "Quản trị viên đã tạo, nhân viên chưa kích hoạt qua liên kết email 48h")
    PENDING_ACTIVATION,

    @Schema(description = "Đang hoạt động bình thường, được phép đăng nhập")
    ACTIVE,

    @Schema(description = "Bị khóa (quản trị viên khóa hoặc tự động khóa khi nhân viên nghỉ việc)")
    LOCKED
}


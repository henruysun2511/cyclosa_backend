package com.cyclosa.auth.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Auth & User (4010 - 4029, 4040 - 4049).
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS         (4011, "Email hoặc mật khẩu không đúng",             HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED               (4012, "Phiên đăng nhập đã hết hạn",                 HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID               (4013, "Token không hợp lệ",                          HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED               (4014, "Token đã bị thu hồi",                         HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED            (4015, "Tài khoản đã bị vô hiệu hóa",                HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID       (4017, "Refresh token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    ACCOUNT_PENDING_ACTIVATION  (4018, "Tài khoản chưa được kích hoạt, vui lòng kiểm tra email để đặt mật khẩu", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED              (4019, "Tài khoản đã bị tạm khóa, vui lòng liên hệ quản trị viên", HttpStatus.FORBIDDEN),
    ACTIVATION_TOKEN_INVALID    (4020, "Mã kích hoạt không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),

    USER_NOT_FOUND              (4040, "Không tìm thấy người dùng",                  HttpStatus.NOT_FOUND),
    USER_EMAIL_EXISTS           (4041, "Email người dùng đã tồn tại",                HttpStatus.CONFLICT),
    USER_ALREADY_LINKED         (4042, "Tài khoản người dùng này đã được gắn cho nhân viên khác", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

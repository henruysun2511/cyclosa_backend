package com.cyclosa.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INTERNAL_ERROR      (5000, "Lỗi hệ thống, vui lòng thử lại sau",             HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED   (4000, "Dữ liệu đầu vào không hợp lệ",                   HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND  (4004, "Không tìm thấy dữ liệu",                          HttpStatus.NOT_FOUND),
    CONFLICT            (4009, "Dữ liệu đã tồn tại",                              HttpStatus.CONFLICT),
    FORBIDDEN           (4003, "Bạn không có quyền thực hiện hành động này",      HttpStatus.FORBIDDEN),
    OPTIMISTIC_LOCK     (4029, "Dữ liệu vừa được cập nhật, vui lòng thử lại",    HttpStatus.CONFLICT),

    UNAUTHORIZED            (4010, "Chưa xác thực, vui lòng đăng nhập",          HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS     (4011, "Email hoặc mật khẩu không đúng",             HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED           (4012, "Phiên đăng nhập đã hết hạn",                 HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID           (4013, "Token không hợp lệ",                          HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED           (4014, "Token đã bị thu hồi",                         HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED        (4015, "Tài khoản đã bị vô hiệu hóa",                HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID   (4017, "Refresh token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),

    USER_NOT_FOUND          (4040, "Không tìm thấy người dùng",                  HttpStatus.NOT_FOUND),
    USER_EMAIL_EXISTS       (4041, "Email người dùng đã tồn tại",                HttpStatus.CONFLICT);

    private final int        code;
    private final String     message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code       = code;
        this.message    = message;
        this.httpStatus = httpStatus;
    }
}

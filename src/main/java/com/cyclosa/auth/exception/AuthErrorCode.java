package com.cyclosa.auth.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi module Xác thực & Tài khoản (Dải mã 1000 - 1099)
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    USER_NOT_FOUND              (1001, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS        (1002, "Email đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS         (1003, "Email hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    ACCOUNT_PENDING_ACTIVATION  (1004, "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email để đặt mật khẩu lần đầu", HttpStatus.UNPROCESSABLE_ENTITY),
    ACCOUNT_LOCKED              (1005, "Tài khoản đã bị khóa. Vui lòng liên hệ Quản trị viên để được hỗ trợ", HttpStatus.LOCKED),
    ACTIVATION_TOKEN_INVALID    (1006, "Mã kích hoạt không hợp lệ hoặc đã hết hạn (48 giờ)", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_INVALID       (1007, "Refresh Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_REVOKED        (1008, "Phiên đăng nhập đã bị thu hồi hoặc đăng xuất", HttpStatus.UNAUTHORIZED),
    CANNOT_RESEND_ACTIVATION    (1009, "Chỉ có thể gửi lại mã kích hoạt cho tài khoản đang ở trạng thái PENDING_ACTIVATION", HttpStatus.BAD_REQUEST),
    GOOGLE_AUTH_FAILED          (1010, "Xác thực tài khoản Google không thành công", HttpStatus.UNAUTHORIZED),
    ACCOUNT_ALREADY_ACTIVE      (1011, "Tài khoản đã được kích hoạt trước đó", HttpStatus.BAD_REQUEST),
    OTP_INVALID                 (1012, "Mã OTP không chính xác", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED                 (1013, "Mã OTP đã hết hạn hoặc chưa được tạo", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED   (1014, "Bạn đã nhập sai OTP quá 5 lần. Vui lòng yêu cầu mã OTP mới", HttpStatus.TOO_MANY_REQUESTS),
    PASSWORD_CONFIRM_MISMATCH   (1015, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_OLD        (1016, "Mật khẩu mới không được trùng với mật khẩu hiện tại", HttpStatus.BAD_REQUEST),
    NO_PASSWORD_SET             (1017, "Tài khoản chưa thiết lập mật khẩu (đăng nhập bằng Google)", HttpStatus.BAD_REQUEST),
    CURRENT_PASSWORD_INCORRECT  (1018, "Mật khẩu hiện tại không chính xác", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS     (1019, "Tên đăng nhập đã tồn tại trong hệ thống", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

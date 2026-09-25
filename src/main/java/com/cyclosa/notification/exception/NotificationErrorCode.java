package com.cyclosa.notification.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Notification (4900 - 4929).
 */
@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND          (4901, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    NOTIFICATION_ACCESS_DENIED      (4902, "Bạn không có quyền thao tác trên thông báo này", HttpStatus.FORBIDDEN),
    RECIPIENT_USER_NOT_FOUND        (4903, "Không tìm thấy tài khoản người nhận thông báo", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

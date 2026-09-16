package com.cyclosa.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi dùng chung toàn hệ thống (4000 - 4009, 5000+).
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_ERROR      (5000, "Lỗi hệ thống, vui lòng thử lại sau",             HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED   (4000, "Dữ liệu đầu vào không hợp lệ",                   HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND  (4004, "Không tìm thấy dữ liệu",                          HttpStatus.NOT_FOUND),
    CONFLICT            (4009, "Dữ liệu đã tồn tại",                              HttpStatus.CONFLICT),
    FORBIDDEN           (4003, "Bạn không có quyền thực hiện hành động này",      HttpStatus.FORBIDDEN),
    UNAUTHORIZED        (4010, "Chưa xác thực, vui lòng đăng nhập",               HttpStatus.UNAUTHORIZED),
    OPTIMISTIC_LOCK     (4029, "Dữ liệu vừa được cập nhật, vui lòng thử lại",    HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

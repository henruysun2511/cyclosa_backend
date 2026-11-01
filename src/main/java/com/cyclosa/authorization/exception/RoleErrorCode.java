package com.cyclosa.authorization.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi module Phân quyền & Quản lý vai trò (Dải mã 3000 - 3099)
 */
@Getter
@RequiredArgsConstructor
public enum RoleErrorCode implements ErrorCode {

    ROLE_NOT_FOUND              (3001, "Không tìm thấy vai trò trong hệ thống", HttpStatus.NOT_FOUND),
    ROLE_CODE_EXISTS            (3002, "Mã vai trò đã tồn tại trong phạm vi công ty", HttpStatus.CONFLICT),
    SYSTEM_ROLE_PROTECTED       (3003, "Không được phép chỉnh sửa mã hoặc xóa vai trò mặc định của hệ thống", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND        (3004, "Không tìm thấy quyền chức năng tương ứng", HttpStatus.NOT_FOUND),
    USER_ROLE_ASSIGNMENT_EXISTS (3005, "Người dùng đã được gán vai trò này tại công ty", HttpStatus.CONFLICT),
    ROLE_IN_USE                 (3006, "Không thể xóa vai trò đang có người dùng nắm giữ", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_NOT_FOUND        (3007, "Không tìm thấy bản ghi phân vai trò của người dùng", HttpStatus.NOT_FOUND),
    DOCUMENT_PERMISSION_NOT_FOUND(3008, "Không tìm thấy cấu hình quyền tài liệu", HttpStatus.NOT_FOUND),
    ACCESS_DENIED_SCOPE         (3009, "Phạm vi dữ liệu của bạn không đủ để truy cập tài nguyên này", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}


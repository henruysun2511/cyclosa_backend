package com.cyclosa.role.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Role & Phân quyền (4030 - 4039).
 */
@Getter
@RequiredArgsConstructor
public enum RoleErrorCode implements ErrorCode {

    ROLE_NOT_FOUND              (4030, "Không tìm thấy vai trò",                                  HttpStatus.NOT_FOUND),
    ROLE_CODE_EXISTS            (4031, "Mã vai trò đã tồn tại",                                  HttpStatus.CONFLICT),
    CANNOT_DELETE_SYSTEM_ROLE   (4032, "Không thể xóa vai trò mặc định của hệ thống",             HttpStatus.FORBIDDEN),
    CANNOT_MODIFY_SYSTEM_ROLE_CODE (4033, "Không thể sửa mã của vai trò mặc định hệ thống",      HttpStatus.FORBIDDEN),
    ROLE_IN_USE                 (4034, "Vai trò đang được gán cho người dùng, không thể xóa",    HttpStatus.CONFLICT),
    PERMISSION_NOT_FOUND        (4035, "Không tìm thấy quyền",                                  HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

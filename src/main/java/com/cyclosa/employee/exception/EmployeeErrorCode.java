package com.cyclosa.employee.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Employee (4200 - 4219).
 */
@Getter
@RequiredArgsConstructor
public enum EmployeeErrorCode implements ErrorCode {

    EMPLOYEE_NOT_FOUND          (4201, "Không tìm thấy hồ sơ nhân viên", HttpStatus.NOT_FOUND),
    EMPLOYEE_CODE_EXISTS        (4202, "Mã nhân viên đã tồn tại trong công ty", HttpStatus.CONFLICT),
    NATIONAL_ID_EXISTS          (4203, "Số CCCD/Hộ chiếu đã được đăng ký cho nhân viên khác", HttpStatus.CONFLICT),
    INVALID_EMPLOYMENT_STATUS   (4204, "Trạng thái nhân viên không hợp lệ cho thao tác này", HttpStatus.BAD_REQUEST),
    DEPENDENT_NOT_FOUND         (4205, "Không tìm thấy thông tin người phụ thuộc", HttpStatus.NOT_FOUND),
    SELF_MANAGER_NOT_ALLOWED    (4206, "Quản lý trực tiếp không thể là chính nhân viên đó", HttpStatus.BAD_REQUEST),
    EMPLOYEE_ACCESS_DENIED      (4207, "Bạn không có quyền truy cập hồ sơ nhân viên này", HttpStatus.FORBIDDEN),
    EMERGENCY_CONTACT_NOT_FOUND (4208, "Không tìm thấy thông tin người liên hệ khẩn cấp", HttpStatus.NOT_FOUND),
    USER_ALREADY_LINKED         (4209, "Tài khoản người dùng này đã được gắn cho nhân viên khác", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

package com.cyclosa.payroll.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi nghiệp vụ của Module Payroll (4401 - 4420).
 */
@Getter
@RequiredArgsConstructor
public enum PayrollErrorCode implements ErrorCode {

    PAYROLL_PERIOD_NOT_FOUND       (4401, "Không tìm thấy kỳ tính lương", HttpStatus.NOT_FOUND),
    PAYROLL_PERIOD_CODE_EXISTS     (4402, "Mã kỳ tính lương đã tồn tại trong công ty", HttpStatus.CONFLICT),
    PAYROLL_PERIOD_LOCKED          (4403, "Kỳ lương đã chốt hoặc đã khóa sổ, không được phép chỉnh sửa", HttpStatus.BAD_REQUEST),
    PAYROLL_NOT_YET_PROCESSED      (4404, "Kỳ tính lương chưa được chạy tính toán dữ liệu", HttpStatus.BAD_REQUEST),
    PAYROLL_ALREADY_APPROVED       (4405, "Bảng lương đã được phê duyệt trước đó", HttpStatus.CONFLICT),
    SALARY_COMPONENT_NOT_FOUND     (4406, "Không tìm thấy thành phần lương", HttpStatus.NOT_FOUND),
    SALARY_COMPONENT_CODE_EXISTS   (4407, "Mã thành phần lương đã tồn tại trong công ty", HttpStatus.CONFLICT),
    SALARY_ADVANCE_EXCEEDS_LIMIT   (4408, "Số tiền xin tạm ứng vượt quá hạn mức tối đa cho phép", HttpStatus.BAD_REQUEST),
    SALARY_ADVANCE_ALREADY_PROCESSED(4409, "Yêu cầu tạm ứng này đã được xử lý", HttpStatus.CONFLICT),
    PAYROLL_RECORD_NOT_FOUND       (4410, "Không tìm thấy bản ghi lương của nhân viên", HttpStatus.NOT_FOUND),
    SALARY_ADVANCE_NOT_FOUND       (4411, "Không tìm thấy đơn tạm ứng tiền lương", HttpStatus.NOT_FOUND),
    CURRENT_USER_NOT_EMPLOYEE      (4412, "Tài khoản hiện tại chưa liên kết với hồ sơ nhân viên", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

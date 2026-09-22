package com.cyclosa.attendance.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Attendance (6001 - 6020).
 */
@Getter
@RequiredArgsConstructor
public enum AttendanceErrorCode implements ErrorCode {

    SHIFT_NOT_FOUND                 (6001, "Không tìm thấy ca làm việc", HttpStatus.NOT_FOUND),
    SHIFT_CODE_EXISTS               (6002, "Mã ca làm việc đã tồn tại trong công ty", HttpStatus.CONFLICT),
    SHIFT_ASSIGNMENT_NOT_FOUND      (6003, "Không tìm thấy lịch phân ca", HttpStatus.NOT_FOUND),
    LOCATION_OUT_OF_RANGE           (6004, "Vị trí chấm công nằm ngoài bán kính cho phép của chi nhánh", HttpStatus.BAD_REQUEST),
    BRANCH_COORDINATES_MISSING      (6005, "Chi nhánh chưa được cấu hình tọa độ GPS hoặc bán kính check-in", HttpStatus.BAD_REQUEST),
    ATTENDANCE_ALREADY_CHECKED_IN   (6006, "Hôm nay bạn đã thực hiện check-in ca này rồi", HttpStatus.BAD_REQUEST),
    ATTENDANCE_NOT_CHECKED_IN       (6007, "Chưa ghi nhận lượt check-in hôm nay", HttpStatus.BAD_REQUEST),
    ATTENDANCE_ALREADY_CHECKED_OUT  (6008, "Lượt check-out hôm nay đã được ghi nhận trước đó", HttpStatus.BAD_REQUEST),
    ATTENDANCE_RECORD_NOT_FOUND     (6009, "Không tìm thấy bản ghi chấm công", HttpStatus.NOT_FOUND),
    EXPLANATION_NOT_FOUND           (6010, "Không tìm thấy đơn giải trình chấm công", HttpStatus.NOT_FOUND),
    EXPLANATION_ALREADY_PROCESSED   (6011, "Đơn giải trình đã được xử lý trước đó", HttpStatus.BAD_REQUEST),
    TIMESHEET_LOCKED                (6012, "Bảng công tháng này đã bị khóa, không thể thay đổi dữ liệu", HttpStatus.BAD_REQUEST),
    EMPLOYEE_HAS_NO_BRANCH          (6013, "Nhân viên chưa được phân bổ chi nhánh làm việc", HttpStatus.BAD_REQUEST),
    SHIFT_HAS_ASSIGNMENTS           (6014, "Không thể xóa ca làm việc đang được phân ca cho nhân viên", HttpStatus.BAD_REQUEST),
    CURRENT_USER_NOT_EMPLOYEE       (6015, "Tài khoản hiện tại chưa liên kết với hồ sơ nhân viên", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

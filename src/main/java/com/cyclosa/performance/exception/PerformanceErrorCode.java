package com.cyclosa.performance.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của Module 09: Quản lý Hiệu suất & Đánh giá KPI (9001 - 9020).
 */
@Getter
@RequiredArgsConstructor
public enum PerformanceErrorCode implements ErrorCode {

    CYCLE_NOT_FOUND                     (9001, "Không tìm thấy kỳ đánh giá hiệu suất", HttpStatus.NOT_FOUND),
    CYCLE_NAME_EXISTS                   (9002, "Tên kỳ đánh giá đã tồn tại trong công ty", HttpStatus.CONFLICT),
    CYCLE_DATE_INVALID                  (9003, "Ngày bắt đầu không được sau ngày kết thúc", HttpStatus.BAD_REQUEST),
    CYCLE_CANNOT_BE_MODIFIED            (9004, "Kỳ đánh giá đã đóng hoặc đã lưu trữ, không thể chỉnh sửa", HttpStatus.CONFLICT),
    KPI_NOT_FOUND                       (9005, "Không tìm thấy chỉ số KPI", HttpStatus.NOT_FOUND),
    KPI_CODE_EXISTS                     (9006, "Mã chỉ số KPI đã tồn tại trong công ty", HttpStatus.CONFLICT),
    GOAL_NOT_FOUND                      (9007, "Không tìm thấy mục tiêu hiệu suất", HttpStatus.NOT_FOUND),
    EVALUATION_NOT_FOUND                (9008, "Không tìm thấy phiếu đánh giá hiệu suất", HttpStatus.NOT_FOUND),
    EVALUATION_ALREADY_EXISTS           (9009, "Phiếu đánh giá cho nhân viên trong kỳ này đã tồn tại", HttpStatus.CONFLICT),
    EVALUATION_ALREADY_FINALIZED        (9010, "Phiếu đánh giá đã được hoàn tất phê duyệt, không thể chỉnh sửa", HttpStatus.CONFLICT),
    SELF_EVALUATION_NOT_SUBMITTED       (9011, "Nhân viên chưa gửi bản tự đánh giá", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS                 (9012, "Bạn không có quyền truy cập hoặc đánh giá phiếu này", HttpStatus.FORBIDDEN),
    WEIGHT_SUM_INVALID                  (9013, "Tổng trọng số các mục tiêu đánh giá phải bằng 100%", HttpStatus.BAD_REQUEST),
    CURRENT_USER_NOT_LINKED_EMPLOYEE    (9014, "Tài khoản hiện tại chưa được liên kết với nhân viên", HttpStatus.BAD_REQUEST),
    EMPLOYEE_NOT_FOUND                  (9015, "Không tìm thấy thông tin nhân viên được chỉ định", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

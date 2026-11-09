package com.cyclosa.workflow.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi nghiệp vụ module Quy trình Phê duyệt (Dải mã 1901 - 1920)
 */
@Getter
@RequiredArgsConstructor
public enum WorkflowErrorCode implements ErrorCode {

    WORKFLOW_DEFINITION_NOT_FOUND     (1901, "Không tìm thấy cấu hình quy trình phê duyệt", HttpStatus.NOT_FOUND),
    WORKFLOW_REQUEST_NOT_FOUND        (1902, "Không tìm thấy hồ sơ yêu cầu phê duyệt", HttpStatus.NOT_FOUND),
    WORKFLOW_STEP_NOT_FOUND           (1903, "Bước phê duyệt không tồn tại", HttpStatus.NOT_FOUND),
    NOT_ASSIGNED_APPROVER             (1904, "Bạn không có thẩm quyền phê duyệt bước này", HttpStatus.FORBIDDEN),
    STEP_NOT_IN_CURRENT_STATE         (1905, "Bước này chưa đến lượt phê duyệt hoặc đã được xử lý xong", HttpStatus.BAD_REQUEST),
    CANNOT_APPROVE_OWN_REQUEST        (1906, "Bạn không được phép tự phê duyệt yêu cầu do chính mình tạo ra", HttpStatus.FORBIDDEN),
    REJECTION_REASON_REQUIRED         (1907, "Vui lòng nhập lý do từ chối yêu cầu phê duyệt (tối thiểu 5 ký tự)", HttpStatus.BAD_REQUEST),
    REQUEST_ALREADY_CANCELLED         (1908, "Yêu cầu phê duyệt này đã bị người gửi rút lại trước đó", HttpStatus.BAD_REQUEST),
    DIRECT_MANAGER_NOT_ASSIGNED       (1909, "Nhân viên chưa được gán Quản lý trực tiếp trên sơ đồ tổ chức", HttpStatus.BAD_REQUEST),
    DELEGATION_DATE_INVALID           (1910, "Ngày kết thúc ủy quyền không được trước ngày bắt đầu", HttpStatus.BAD_REQUEST),
    CANNOT_DELEGATE_TO_SELF           (1911, "Không thể tự ủy quyền cho chính mình", HttpStatus.BAD_REQUEST),
    DELEGATION_NOT_FOUND              (1912, "Không tìm thấy thông tin ủy quyền", HttpStatus.NOT_FOUND),
    WORKFLOW_STEPS_EMPTY              (1913, "Quy trình phê duyệt phải chứa ít nhất 01 bước", HttpStatus.BAD_REQUEST),
    WORKFLOW_ALREADY_ACTIVE           (1914, "Đã có một quy trình khác cùng loại đang được áp dụng trong công ty", HttpStatus.CONFLICT),
    REQUEST_ALREADY_SUBMITTED         (1915, "Chứng từ này đã có một yêu cầu phê duyệt đang chờ xử lý", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}


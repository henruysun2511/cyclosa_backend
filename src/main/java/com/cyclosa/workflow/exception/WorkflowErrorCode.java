package com.cyclosa.workflow.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Workflow Engine (4800 - 4829).
 */
@Getter
@RequiredArgsConstructor
public enum WorkflowErrorCode implements ErrorCode {

    WORKFLOW_DEFINITION_NOT_FOUND   (4801, "Không tìm thấy định nghĩa quy trình phê duyệt", HttpStatus.NOT_FOUND),
    WORKFLOW_INSTANCE_NOT_FOUND     (4802, "Không tìm thấy phiên phê duyệt", HttpStatus.NOT_FOUND),
    NO_PUBLISHED_WORKFLOW           (4803, "Không có quy trình phê duyệt nào đang được kích hoạt cho loại yêu cầu này", HttpStatus.UNPROCESSABLE_ENTITY),
    WORKFLOW_ALREADY_CLOSED         (4804, "Phiên phê duyệt đã kết thúc hoặc đã bị đóng", HttpStatus.UNPROCESSABLE_ENTITY),
    NOT_AUTHORIZED_APPROVER         (4805, "Bạn không có quyền thực hiện duyệt hoặc từ chối tại bước này", HttpStatus.FORBIDDEN),
    STEP_ALREADY_ACTED              (4806, "Bước phê duyệt này đã được xử lý trước đó", HttpStatus.CONFLICT),
    CANNOT_DELEGATE_TO_SELF         (4807, "Không thể ủy quyền phê duyệt cho chính bản thân mình", HttpStatus.UNPROCESSABLE_ENTITY),
    OVERLAPPING_DELEGATION          (4808, "Thời gian ủy quyền bị trùng lặp với một ủy quyền đang có hiệu lực", HttpStatus.CONFLICT),
    APPROVER_NOT_RESOLVED           (4809, "Không thể xác định được người phê duyệt hợp lệ cho bước này", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_CONDITION_EXPRESSION    (4810, "Biểu thức điều kiện rẽ nhánh không hợp lệ hoặc sai cú pháp", HttpStatus.BAD_REQUEST),
    CANNOT_MODIFY_PUBLISHED_WORKFLOW(4811, "Không thể chỉnh sửa trực tiếp quy trình đã xuất bản. Vui lòng nhân bản để tạo bản nháp mới", HttpStatus.BAD_REQUEST),
    WORKFLOW_STEP_NOT_FOUND         (4812, "Không tìm thấy bước quy trình yêu cầu", HttpStatus.NOT_FOUND),
    INVALID_DELEGATION_DATE         (4813, "Ngày kết thúc ủy quyền không thể trước ngày bắt đầu", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

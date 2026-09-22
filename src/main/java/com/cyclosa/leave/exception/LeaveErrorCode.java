package com.cyclosa.leave.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi nghiệp vụ của Module Leave (7001 - 7020).
 */
@Getter
@RequiredArgsConstructor
public enum LeaveErrorCode implements ErrorCode {

    LEAVE_TYPE_NOT_FOUND            (7001, "Không tìm thấy loại ngày nghỉ", HttpStatus.NOT_FOUND),
    LEAVE_TYPE_CODE_EXISTS          (7002, "Mã loại ngày nghỉ đã tồn tại trong công ty", HttpStatus.CONFLICT),
    LEAVE_POLICY_NOT_FOUND          (7003, "Không tìm thấy chính sách nghỉ phép", HttpStatus.NOT_FOUND),
    LEAVE_POLICY_EXISTS             (7004, "Chính sách nghỉ phép cho điều kiện này đã tồn tại", HttpStatus.CONFLICT),
    LEAVE_BALANCE_NOT_FOUND         (7005, "Không tìm thấy quỹ phép của nhân viên cho năm này", HttpStatus.NOT_FOUND),
    LEAVE_REQUEST_NOT_FOUND         (7006, "Không tìm thấy đơn xin nghỉ phép", HttpStatus.NOT_FOUND),
    INSUFFICIENT_LEAVE_BALANCE      (7007, "Số dư phép khả dụng không đủ để thực hiện yêu cầu này", HttpStatus.UNPROCESSABLE_ENTITY),
    OVERLAPPING_LEAVE_REQUEST       (7008, "Khoảng thời gian nghỉ phép bị trùng lặp với đơn xin nghỉ khác đã gửi", HttpStatus.CONFLICT),
    LEAVE_DATE_IN_PAST              (7009, "Ngày bắt đầu nghỉ phép không được ở trong quá khứ", HttpStatus.UNPROCESSABLE_ENTITY),
    CANNOT_REQUEST_LEAVE_ON_WORKED_DAY(7010, "Không thể xin nghỉ phép vào ngày đã có dữ liệu chấm công đi làm", HttpStatus.UNPROCESSABLE_ENTITY),
    LEAVE_ALREADY_STARTED           (7011, "Đơn nghỉ phép đã đến hoặc qua ngày bắt đầu nghỉ, không thể hủy", HttpStatus.UNPROCESSABLE_ENTITY),
    LEAVE_REQUEST_ALREADY_PROCESSED (7012, "Đơn nghỉ phép đã được xử lý trước đó", HttpStatus.BAD_REQUEST),
    CURRENT_USER_NOT_EMPLOYEE       (7013, "Tài khoản hiện tại chưa liên kết với hồ sơ nhân viên", HttpStatus.BAD_REQUEST),
    LEAVE_TYPE_IN_USE               (7014, "Loại ngày nghỉ đang có đơn hoặc chính sách liên kết, không thể xóa", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE              (7015, "Khoảng thời gian nghỉ không hợp lệ: ngày kết thúc phải sau hoặc bằng ngày bắt đầu", HttpStatus.BAD_REQUEST),
    LEAVE_BALANCE_ALREADY_EXISTS    (7016, "Quỹ phép của nhân viên cho loại nghỉ và năm này đã tồn tại", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

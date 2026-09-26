package com.cyclosa.offboarding.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của Module 16: Thôi việc & Bàn giao (16001 - 16020).
 */
@Getter
@RequiredArgsConstructor
public enum OffboardingErrorCode implements ErrorCode {

    RESIGNATION_NOT_FOUND               (16001, "Không tìm thấy đơn xin thôi việc", HttpStatus.NOT_FOUND),
    TERMINATION_NOT_FOUND               (16002, "Không tìm thấy quyết định chấm dứt hợp đồng lao động", HttpStatus.NOT_FOUND),
    CLEARANCE_NOT_FOUND                 (16003, "Không tìm thấy thông tin thủ tục bàn giao", HttpStatus.NOT_FOUND),
    EXIT_INTERVIEW_NOT_FOUND            (16004, "Không tìm thấy bản phỏng vấn thôi việc của nhân viên", HttpStatus.NOT_FOUND),
    RESIGNATION_ALREADY_PROCESSED       (16005, "Đơn xin thôi việc này đã được xử lý phê duyệt hoặc từ chối trước đó", HttpStatus.CONFLICT),
    TERMINATION_ALREADY_PROCESSED       (16006, "Quyết định chấm dứt hợp đồng này đã được xử lý trước đó", HttpStatus.CONFLICT),
    UNRETURNED_ASSETS_EXIST             (16007, "Nhân viên vẫn còn tài sản thiết bị chưa hoàn trả, không thể hoàn tất thôi việc", HttpStatus.UNPROCESSABLE_ENTITY),
    CLEARANCES_INCOMPLETE               (16008, "Chưa hoàn tất đầy đủ các thủ tục bàn giao (Clearance) của các bộ phận liên quan", HttpStatus.UNPROCESSABLE_ENTITY),
    OFFBOARDING_ALREADY_COMPLETED       (16009, "Thủ tục thôi việc của nhân viên này đã hoàn tất trước đó", HttpStatus.CONFLICT),
    CURRENT_USER_NOT_LINKED_EMPLOYEE    (16010, "Tài khoản hiện tại chưa được liên kết với hồ sơ nhân viên trong hệ thống", HttpStatus.BAD_REQUEST),
    EMPLOYEE_NOT_FOUND                  (16011, "Không tìm thấy thông tin nhân viên", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_OFFBOARDING_ACCESS     (16012, "Bạn không có quyền thao tác trên hồ sơ thôi việc này", HttpStatus.FORBIDDEN),
    INSUFFICIENT_NOTICE_PERIOD          (16013, "Thời gian báo trước không đủ theo quy định của hợp đồng và Điều 35 Bộ luật Lao động", HttpStatus.UNPROCESSABLE_ENTITY),
    OFFBOARDING_ALREADY_IN_PROGRESS     (16014, "Nhân viên đã có đơn thôi việc hoặc quyết định chấm dứt hợp đồng đang trong tiến trình xử lý", HttpStatus.CONFLICT),
    NO_APPROVED_OFFBOARDING             (16015, "Nhân viên không có đơn xin thôi việc hoặc quyết định chấm dứt hợp đồng nào ở trạng thái đã được phê duyệt", HttpStatus.UNPROCESSABLE_ENTITY),
    REJECTION_REASON_REQUIRED           (16016, "Lý do từ chối đơn thôi việc là bắt buộc khi không phê duyệt", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

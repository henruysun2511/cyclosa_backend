package com.cyclosa.discipline.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của Module 12: Khen thưởng & Kỷ luật (12001 - 12020).
 */
@Getter
@RequiredArgsConstructor
public enum DisciplineErrorCode implements ErrorCode {

    DISCIPLINE_NOT_FOUND                (12001, "Không tìm thấy hồ sơ xử lý kỷ luật", HttpStatus.NOT_FOUND),
    REWARD_NOT_FOUND                    (12002, "Không tìm thấy quyết định khen thưởng", HttpStatus.NOT_FOUND),
    GRIEVANCE_NOT_FOUND                 (12003, "Không tìm thấy đơn khiếu nại, phản ánh", HttpStatus.NOT_FOUND),
    REWARD_ALREADY_PUSHED_TO_PAYROLL    (12004, "Khoản khen thưởng đã được chốt và đẩy vào bảng lương, không thể sửa hoặc xóa", HttpStatus.CONFLICT),
    DISCIPLINE_ALREADY_APPROVED         (12005, "Biên bản kỷ luật này đã được phê duyệt quyết định trước đó", HttpStatus.CONFLICT),
    STATUTE_OF_LIMITATIONS_EXPIRED      (12006, "Đã quá thời hiệu xử lý kỷ luật lao động theo quy định của Bộ luật Lao động", HttpStatus.UNPROCESSABLE_ENTITY),
    DISMISSAL_REQUIRES_GROUND           (12007, "Kỷ luật sa thải bắt buộc phải có căn cứ theo Điều 125 Bộ luật Lao động", HttpStatus.BAD_REQUEST),
    SALARY_EXTENSION_MONTHS_INVALID     (12008, "Thời hạn kéo dài nâng bậc lương không được vượt quá 6 tháng theo Điều 124 BLLĐ", HttpStatus.BAD_REQUEST),
    GRIEVANCE_ALREADY_RESOLVED          (12009, "Đơn khiếu nại này đã được giải quyết và đóng hồ sơ", HttpStatus.CONFLICT),
    CURRENT_USER_NOT_LINKED_EMPLOYEE    (12010, "Tài khoản hiện tại chưa được liên kết với hồ sơ nhân viên trong hệ thống", HttpStatus.BAD_REQUEST),
    EMPLOYEE_NOT_FOUND                  (12011, "Không tìm thấy thông tin nhân viên được chỉ định", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_DISCIPLINE_ACCESS      (12012, "Bạn không có quyền truy cập hồ sơ kỷ luật / khen thưởng này", HttpStatus.FORBIDDEN),
    DISCIPLINE_RESTRICTED_BY_LAW        (12013, "Không được xử lý kỷ luật lao động đối với nhân viên đang nghỉ phép, nghỉ ốm, thai sản hoặc nuôi con dưới 12 tháng tuổi theo Điều 122.4 BLLĐ", HttpStatus.UNPROCESSABLE_ENTITY),
    TERMINATION_REQUIRES_APPROVAL       (12014, "Kỷ luật sa thải bắt buộc phải qua Workflow phê duyệt cấp cao trước khi ban hành", HttpStatus.UNPROCESSABLE_ENTITY),
    DISCIPLINE_PROCEDURE_INCOMPLETE     (12015, "Kỷ luật lao động bắt buộc phải có đầy đủ căn cứ nội quy, bằng chứng vi phạm, ngày họp và thành phần tham dự theo Điều 122 BLLĐ", HttpStatus.UNPROCESSABLE_ENTITY),
    EMPLOYEE_NOT_ACTIVE                 (12016, "Nhân viên không ở trạng thái đang làm việc", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

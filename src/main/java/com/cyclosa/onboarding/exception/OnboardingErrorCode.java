package com.cyclosa.onboarding.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OnboardingErrorCode implements ErrorCode {
    ONBOARDING_PROCESS_NOT_FOUND        (5201, "Không tìm thấy tiến trình onboarding", HttpStatus.NOT_FOUND),
    ONBOARDING_ALREADY_IN_PROGRESS      (5202, "Nhân viên đã có quy trình onboarding đang diễn ra", HttpStatus.CONFLICT),
    CHECKLIST_TEMPLATE_NOT_FOUND        (5203, "Không tìm thấy mẫu danh sách onboarding", HttpStatus.NOT_FOUND),
    PROCESS_ITEM_NOT_FOUND              (5204, "Không tìm thấy hạng mục công việc onboarding", HttpStatus.NOT_FOUND),
    ONBOARDING_PROCESS_CLOSED           (5205, "Quy trình onboarding đã kết thúc hoặc bị hủy, không thể chỉnh sửa", HttpStatus.UNPROCESSABLE_ENTITY),
    ITEM_ALREADY_COMPLETED              (5206, "Hạng mục công việc đã được hoàn thành trước đó", HttpStatus.UNPROCESSABLE_ENTITY),
    ACCOUNT_NOT_PROVISIONED_YET         (5207, "Cần cấp phát tài khoản tương ứng ở trạng thái đã cấp (PROVISIONED) trước khi hoàn thành", HttpStatus.UNPROCESSABLE_ENTITY),
    REQUIRED_ITEMS_NOT_COMPLETED        (5208, "Các hạng mục bắt buộc chưa hoàn thành đầy đủ", HttpStatus.UNPROCESSABLE_ENTITY),
    ACCOUNT_PROVISIONING_NOT_FOUND      (5209, "Không tìm thấy thông tin cấp phát tài khoản", HttpStatus.NOT_FOUND),
    ORIENTATION_SESSION_NOT_FOUND       (5210, "Không tìm thấy buổi đào tạo định hướng", HttpStatus.NOT_FOUND),
    EMPLOYEE_DOCUMENT_NOT_FOUND         (5211, "Không tìm thấy tài liệu hồ sơ nhân viên", HttpStatus.NOT_FOUND),
    TEMPLATE_ITEM_NOT_FOUND             (5212, "Không tìm thấy hạng mục trong mẫu checklist", HttpStatus.NOT_FOUND),
    ONBOARDING_CANNOT_CANCEL            (5213, "Quy trình onboarding đã hoàn thành, không thể hủy", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

package com.cyclosa.employee.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi nghiệp vụ module Hồ sơ nhân sự (Dải mã 1501 - 1520)
 */
@Getter
@RequiredArgsConstructor
public enum EmployeeErrorCode implements ErrorCode {

    EMPLOYEE_NOT_FOUND             (1501, "Không tìm thấy hồ sơ nhân viên yêu cầu", HttpStatus.NOT_FOUND),
    EMPLOYEE_CODE_EXISTS           (1502, "Mã nhân viên đã tồn tại trên hệ thống", HttpStatus.CONFLICT),
    IDENTITY_DOCUMENT_EXISTS       (1503, "Số CCCD hoặc Hộ chiếu đã được đăng ký cho một nhân viên khác", HttpStatus.CONFLICT),
    TAX_OR_INSURANCE_EXISTS        (1504, "Mã số thuế cá nhân hoặc mã số BHXH đã bị trùng lặp", HttpStatus.CONFLICT),
    SELF_MANAGEMENT_NOT_ALLOWED    (1505, "Nhân viên không thể tự làm người quản lý trực tiếp của chính mình", HttpStatus.BAD_REQUEST),
    NO_VALID_WORK_ASSIGNMENT       (1506, "Không tìm thấy phân công công tác nào có hiệu lực tại ngày chỉ định", HttpStatus.BAD_REQUEST),
    EFFECTIVE_DATE_INVALID         (1507, "Ngày có hiệu lực của công tác mới phải lớn hơn ngày phân công gần nhất", HttpStatus.BAD_REQUEST),
    PROBATION_NOT_PASSED           (1508, "Chưa đủ điều kiện chuyển chính thức: Chưa đạt đánh giá thử việc", HttpStatus.UNPROCESSABLE_ENTITY),
    ONBOARDING_NOT_COMPLETED       (1509, "Chưa đủ điều kiện chuyển chính thức: Chưa hoàn tất các hạng mục hội nhập bắt buộc", HttpStatus.UNPROCESSABLE_ENTITY),
    OFFICIAL_CONTRACT_MISSING      (1510, "Chưa đủ điều kiện chuyển chính thức: Chưa có hợp đồng lao động chính thức", HttpStatus.UNPROCESSABLE_ENTITY),
    EMPLOYEE_ALREADY_TERMINATED    (1511, "Nhân viên đã ở trạng thái thôi việc, không thể thay đổi dữ liệu công tác", HttpStatus.BAD_REQUEST),
    IDENTITY_DOCUMENT_NOT_FOUND    (1512, "Không tìm thấy giấy tờ định danh yêu cầu", HttpStatus.NOT_FOUND),
    BANK_ACCOUNT_NOT_FOUND         (1513, "Không tìm thấy thông tin tài khoản ngân hàng của nhân viên", HttpStatus.NOT_FOUND),
    DEPENDENT_NOT_FOUND            (1514, "Không tìm thấy bản ghi người phụ thuộc yêu cầu", HttpStatus.NOT_FOUND),
    DEDUCTION_DATE_INVALID         (1515, "Ngày kết thúc giảm trừ thuế phải lớn hơn hoặc bằng ngày bắt đầu giảm trừ", HttpStatus.BAD_REQUEST),
    EMERGENCY_CONTACT_NOT_FOUND    (1516, "Không tìm thấy người liên hệ khẩn cấp", HttpStatus.NOT_FOUND),
    FACE_QUALITY_TOO_LOW           (1517, "Điểm chất lượng ảnh sinh trắc khuôn mặt không đạt ngưỡng tối thiểu", HttpStatus.BAD_REQUEST),
    FACE_LIVENESS_FAILED           (1518, "Xác thực thực thể sống (Liveness check) thất bại", HttpStatus.BAD_REQUEST),
    FACE_CONSENT_REQUIRED          (1519, "Chưa có sự đồng thuận của người lao động đối với dữ liệu sinh trắc", HttpStatus.BAD_REQUEST),
    FACE_ENROLLMENT_NOT_FOUND      (1520, "Không tìm thấy mẫu đăng ký khuôn mặt của nhân viên", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}


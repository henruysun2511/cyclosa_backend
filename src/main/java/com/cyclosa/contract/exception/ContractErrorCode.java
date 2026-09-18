package com.cyclosa.contract.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Contract (4300 - 4340).
 */
@Getter
@RequiredArgsConstructor
public enum ContractErrorCode implements ErrorCode {

    CONTRACT_NOT_FOUND                      (4301, "Không tìm thấy hợp đồng lao động", HttpStatus.NOT_FOUND),
    CONTRACT_NUMBER_EXISTS                  (4302, "Số hợp đồng đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    ACTIVE_CONTRACT_ALREADY_EXISTS          (4303, "Nhân viên hiện đang có hợp đồng lao động có hiệu lực", HttpStatus.CONFLICT),
    CONTRACT_DURATION_EXCEEDS_LIMIT         (4304, "Thời hạn hợp đồng xác định thời hạn không được vượt quá 36 tháng (Điều 20 BLLĐ 2019)", HttpStatus.UNPROCESSABLE_ENTITY),
    MAX_DEFINITE_CONTRACTS_EXCEEDED         (4305, "Đã ký đủ 02 lần HĐ xác định thời hạn. Lần tiếp theo bắt buộc phải ký HĐ không xác định thời hạn (Điều 20.2 BLLĐ 2019)", HttpStatus.UNPROCESSABLE_ENTITY),
    PROBATION_DURATION_EXCEEDS_LIMIT        (4306, "Thời gian thử việc vượt quá giới hạn luật định theo cấp bậc (Điều 25 BLLĐ 2019)", HttpStatus.UNPROCESSABLE_ENTITY),
    PROBATION_SALARY_BELOW_LEGAL_MINIMUM    (4307, "Tiền lương thử việc phải đạt ít nhất 85% lương chính thức (Điều 26 BLLĐ 2019)", HttpStatus.UNPROCESSABLE_ENTITY),
    PROBATION_ALREADY_COMPLETED             (4308, "Nhân sự chỉ được thử việc 01 lần duy nhất cho một vị trí công việc (Điều 24.2 BLLĐ 2019)", HttpStatus.UNPROCESSABLE_ENTITY),
    TERMINATION_RESTRICTED_BY_LAW           (4309, "Cấm NSDLĐ đơn phương chấm dứt HĐ với nhân sự đang nghỉ ốm đau, thai sản hoặc nuôi con dưới 12 tháng (Điều 37 & 137.3 BLLĐ 2019)", HttpStatus.UNPROCESSABLE_ENTITY),
    CANNOT_MODIFY_ACTIVE_CONTRACT           (4310, "Hợp đồng đang có hiệu lực không thể sửa trực tiếp. Vui lòng lập Phụ lục hợp đồng (Điều 22 BLLĐ 2019)", HttpStatus.BAD_REQUEST),
    INVALID_CONTRACT_STATUS_TRANSITION      (4311, "Chuyển đổi trạng thái hợp đồng không hợp lệ", HttpStatus.BAD_REQUEST),
    ADDENDUM_NOT_FOUND                      (4312, "Không tìm thấy phụ lục hợp đồng", HttpStatus.NOT_FOUND),
    ADDENDUM_NUMBER_EXISTS                  (4313, "Số phụ lục hợp đồng đã tồn tại cho hợp đồng này", HttpStatus.CONFLICT),
    CONTRACT_TEMPLATE_NOT_FOUND             (4314, "Không tìm thấy mẫu hợp đồng yêu cầu", HttpStatus.NOT_FOUND),
    TEMPLATE_CODE_EXISTS                    (4315, "Mã mẫu hợp đồng đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    CONTRACT_ALREADY_TERMINATED             (4316, "Hợp đồng này đã chấm dứt trước đó", HttpStatus.BAD_REQUEST),
    INDEFINITE_CONTRACT_CANNOT_HAVE_END_DATE(4317, "Hợp đồng không xác định thời hạn không được có ngày kết thúc", HttpStatus.BAD_REQUEST),
    DEFINITE_CONTRACT_REQUIRES_END_DATE     (4318, "Hợp đồng xác định thời hạn bắt buộc phải có ngày kết thúc", HttpStatus.BAD_REQUEST),
    INVALID_NOTICE_PERIOD                   (4319, "Vi phạm thời hạn báo trước theo luật định khi chấm dứt hợp đồng (Điều 35, 36 BLLĐ 2019)", HttpStatus.UNPROCESSABLE_ENTITY);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

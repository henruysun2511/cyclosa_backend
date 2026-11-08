package com.cyclosa.contract.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi nghiệp vụ module Hợp đồng lao động & Ký điện tử (Dải mã 1801 - 1820)
 */
@Getter
@RequiredArgsConstructor
public enum ContractErrorCode implements ErrorCode {

    CONTRACT_NOT_FOUND                     (1801, "Không tìm thấy hợp đồng lao động yêu cầu", HttpStatus.NOT_FOUND),
    CONTRACT_TEMPLATE_NOT_FOUND            (1802, "Không tìm thấy biểu mẫu hợp đồng yêu cầu", HttpStatus.NOT_FOUND),
    CONTRACT_TEMPLATE_VERSION_NOT_FOUND    (1803, "Không tìm thấy phiên bản tệp mẫu yêu cầu", HttpStatus.NOT_FOUND),
    CONTRACT_NO_ALREADY_EXISTS             (1804, "Số hiệu hợp đồng đã tồn tại trên hệ thống", HttpStatus.CONFLICT),
    INVALID_CONTRACT_DATES                 (1805, "Ngày kết thúc hợp đồng phải sau ngày bắt đầu", HttpStatus.BAD_REQUEST),
    INDEFINITE_CONTRACT_CANNOT_HAVE_END_DATE(1806, "Hợp đồng không xác định thời hạn không được có ngày kết thúc", HttpStatus.BAD_REQUEST),
    MAX_FIXED_TERM_CONTRACTS_EXCEEDED      (1807, "Đã đạt giới hạn tối đa 02 lần ký HĐ xác định thời hạn theo Điều 20 BLLĐ 2019", HttpStatus.CONFLICT),
    CANNOT_MODIFY_SIGNED_CONTRACT          (1808, "Không thể sửa đổi trực tiếp hợp đồng đã ký hoặc đang trình ký", HttpStatus.BAD_REQUEST),
    APPENDIX_CANNOT_CHANGE_TERM            (1809, "Phụ lục hợp đồng không được phép thay đổi thời hạn hợp đồng", HttpStatus.BAD_REQUEST),
    PROBATION_SALARY_BELOW_LEGAL_MIN       (1810, "Tiền lương thử việc phải đạt ít nhất 85% mức lương chính thức", HttpStatus.BAD_REQUEST),
    INVALID_SIGNING_ORDER                  (1811, "Chưa đến lượt ký của bên này theo quy định thứ tự ký kết", HttpStatus.BAD_REQUEST),
    SIGNER_ALREADY_SIGNED                  (1812, "Bên ký này đã hoàn tất ký, không thể ký lại", HttpStatus.CONFLICT),
    OTP_EXPIRED_OR_INVALID                 (1813, "Mã xác thực OTP không chính xác hoặc đã hết thời gian hiệu lực", HttpStatus.BAD_REQUEST),
    DIGITAL_SIGNATURE_INVALID              (1814, "Chữ ký số không hợp lệ hoặc chứng thư số đã bị thu hồi", HttpStatus.BAD_REQUEST),
    CONTRACT_DOCUMENT_HASH_MISMATCH        (1815, "Tệp văn bản đã bị sửa đổi trái phép (sai lệch mã băm SHA-256)", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_COMPLETED_CONTRACT       (1816, "Hợp đồng đã ký kết hoàn tất không thể hủy bỏ đơn phương", HttpStatus.BAD_REQUEST),
    APPENDIX_NOT_FOUND                     (1817, "Không tìm thấy phụ lục hợp đồng yêu cầu", HttpStatus.NOT_FOUND),
    VERSION_NOT_FOUND                      (1818, "Không tìm thấy phiên bản văn bản hợp đồng yêu cầu", HttpStatus.NOT_FOUND),
    SIGNER_NOT_FOUND                       (1819, "Không tìm thấy thông tin bên ký kết yêu cầu", HttpStatus.NOT_FOUND),
    CONTRACT_NOT_IN_DRAFT                  (1820, "Chỉ có thể sửa đổi hợp đồng khi ở trạng thái DRAFT", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}


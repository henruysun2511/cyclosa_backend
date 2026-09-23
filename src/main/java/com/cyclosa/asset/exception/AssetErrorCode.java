package com.cyclosa.asset.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi nghiệp vụ của Module Asset Management (13001 - 13020).
 */
@Getter
@RequiredArgsConstructor
public enum AssetErrorCode implements ErrorCode {

    ASSET_NOT_FOUND                 (13001, "Không tìm thấy tài sản", HttpStatus.NOT_FOUND),
    ASSET_CODE_ALREADY_EXISTS       (13002, "Mã tài sản đã tồn tại trong công ty", HttpStatus.CONFLICT),
    ASSET_NOT_AVAILABLE             (13003, "Tài sản không ở trạng thái sẵn sàng để cấp phát (phải là IN_STOCK)", HttpStatus.UNPROCESSABLE_ENTITY),
    ASSET_ALREADY_ALLOCATED         (13004, "Tài sản đang được cấp phát cho nhân viên khác", HttpStatus.CONFLICT),
    EMPLOYEE_NOT_FOUND              (13005, "Không tìm thấy thông tin nhân viên", HttpStatus.NOT_FOUND),
    EMPLOYEE_NOT_ACTIVE             (13006, "Nhân viên không ở trạng thái hoạt động", HttpStatus.UNPROCESSABLE_ENTITY),
    NO_OPEN_ALLOCATION_FOUND        (13007, "Không tìm thấy lượt cấp phát đang mở cho tài sản này", HttpStatus.UNPROCESSABLE_ENTITY),
    ALLOCATION_NOT_FOUND            (13008, "Không tìm thấy thông tin cấp phát tài sản", HttpStatus.NOT_FOUND),
    INVENTORY_CHECK_NOT_FOUND       (13009, "Không tìm thấy đợt kiểm kê tài sản", HttpStatus.NOT_FOUND),
    CANNOT_DISPOSE_ALLOCATED_ASSET  (13010, "Không thể thanh lý tài sản đang được cấp phát", HttpStatus.UNPROCESSABLE_ENTITY),
    CANNOT_REPAIR_ALLOCATED_ASSET   (13011, "Không thể chuyển sang bảo trì tài sản đang được cấp phát", HttpStatus.UNPROCESSABLE_ENTITY),
    RETURN_DATE_BEFORE_ALLOCATED_DATE(13012, "Ngày thu hồi không được trước ngày cấp phát", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_ASSET_STATUS_TRANSITION (13013, "Chuyển trạng thái tài sản không hợp lệ", HttpStatus.BAD_REQUEST),
    CONDITION_ON_RETURN_REQUIRED    (13014, "Bắt buộc phải cung cấp tình trạng tài sản khi thu hồi", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

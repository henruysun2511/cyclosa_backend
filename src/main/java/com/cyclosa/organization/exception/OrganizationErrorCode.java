package com.cyclosa.organization.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của module Organization (4100 - 4119).
 */
@Getter
@RequiredArgsConstructor
public enum OrganizationErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND               (4101, "Không tìm thấy thông tin công ty", HttpStatus.NOT_FOUND),
    COMPANY_CODE_EXISTS             (4102, "Mã công ty đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    ORG_UNIT_NOT_FOUND              (4103, "Không tìm thấy đơn vị tổ chức/phòng ban", HttpStatus.NOT_FOUND),
    ORG_UNIT_CODE_EXISTS            (4104, "Mã đơn vị đã tồn tại trong công ty", HttpStatus.CONFLICT),
    CIRCULAR_PARENT_DEPENDENCY      (4105, "Không thể di chuyển đơn vị vào chính nó hoặc cấp dưới của nó", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_UNIT_WITH_CHILDREN(4106, "Không thể xóa đơn vị đang có đơn vị cấp dưới", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_UNIT_WITH_EMPLOYEES(4107, "Không thể xóa đơn vị đang có nhân sự trực thuộc", HttpStatus.BAD_REQUEST),
    REGION_NOT_FOUND                (4108, "Không tìm thấy vùng miền", HttpStatus.NOT_FOUND),
    BRANCH_NOT_FOUND                (4109, "Không tìm thấy chi nhánh", HttpStatus.NOT_FOUND),
    BRANCH_CODE_EXISTS              (4110, "Mã chi nhánh đã tồn tại trong công ty", HttpStatus.CONFLICT),
    JOB_LEVEL_NOT_FOUND             (4111, "Không tìm thấy cấp bậc công việc", HttpStatus.NOT_FOUND),
    JOB_LEVEL_CODE_EXISTS           (4112, "Mã cấp bậc công việc đã tồn tại", HttpStatus.CONFLICT),
    POSITION_NOT_FOUND              (4113, "Không tìm thấy vị trí chức danh", HttpStatus.NOT_FOUND),
    POSITION_CODE_EXISTS            (4114, "Mã vị trí chức danh đã tồn tại trong công ty", HttpStatus.CONFLICT),
    COST_CENTER_NOT_FOUND           (4115, "Không tìm thấy trung tâm chi phí", HttpStatus.NOT_FOUND),
    COST_CENTER_CODE_EXISTS         (4116, "Mã trung tâm chi phí đã tồn tại", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

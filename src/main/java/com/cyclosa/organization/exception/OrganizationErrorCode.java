package com.cyclosa.organization.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi module Quản lý tổ chức (Dải mã 1401 - 1418)
 */
@Getter
@RequiredArgsConstructor
public enum OrganizationErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND              (1401, "Không tìm thấy công ty / pháp nhân", HttpStatus.NOT_FOUND),
    COMPANY_CODE_EXISTS            (1402, "Mã công ty đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    REGION_NOT_FOUND               (1403, "Không tìm thấy khu vực / vùng miền", HttpStatus.NOT_FOUND),
    REGION_CODE_EXISTS             (1404, "Mã khu vực đã tồn tại trong công ty", HttpStatus.CONFLICT),
    BRANCH_NOT_FOUND               (1405, "Không tìm thấy chi nhánh / văn phòng", HttpStatus.NOT_FOUND),
    BRANCH_CODE_EXISTS             (1406, "Mã chi nhánh đã tồn tại trong khu vực", HttpStatus.CONFLICT),
    COST_CENTER_NOT_FOUND          (1407, "Không tìm thấy trung tâm chi phí", HttpStatus.NOT_FOUND),
    COST_CENTER_CODE_EXISTS        (1408, "Mã trung tâm chi phí đã tồn tại", HttpStatus.CONFLICT),
    JOB_TITLE_NOT_FOUND            (1409, "Không tìm thấy chức danh công việc", HttpStatus.NOT_FOUND),
    JOB_TITLE_CODE_EXISTS          (1410, "Mã chức danh đã tồn tại", HttpStatus.CONFLICT),
    JOB_LEVEL_NOT_FOUND            (1411, "Không tìm thấy cấp bậc chức danh", HttpStatus.NOT_FOUND),
    JOB_LEVEL_RANK_EXISTS          (1412, "Thứ tự cấp bậc đã tồn tại", HttpStatus.CONFLICT),
    DEPARTMENT_NOT_FOUND           (1413, "Không tìm thấy phòng ban / đơn vị", HttpStatus.NOT_FOUND),
    DEPARTMENT_CODE_EXISTS         (1414, "Mã phòng ban đã tồn tại trong công ty", HttpStatus.CONFLICT),
    ORG_CYCLE_DETECTED             (1415, "Phát hiện vòng lặp phân cấp: Không thể chuyển đơn vị vào chính nó hoặc nhánh con của nó", HttpStatus.BAD_REQUEST),
    DEPARTMENT_HAS_ACTIVE_MEMBERS  (1416, "Phòng ban vẫn còn nhân sự hoạt động, không thể giải thể", HttpStatus.BAD_REQUEST),
    DEPARTMENT_HAS_CHILDREN        (1417, "Phòng ban vẫn còn các đơn vị cấp dưới trực thuộc, không thể giải thể", HttpStatus.BAD_REQUEST),
    EFFECTIVE_DATE_INVALID         (1418, "Ngày hiệu lực của phiên bản mới phải lớn hơn ngày hiệu lực phiên bản hiện tại", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}


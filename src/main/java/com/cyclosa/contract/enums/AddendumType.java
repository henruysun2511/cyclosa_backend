package com.cyclosa.contract.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Phân loại Phụ lục Hợp đồng lao động theo Điều 22 BLLĐ 2019.
 */
@Getter
@RequiredArgsConstructor
public enum AddendumType {
    SALARY_ADJUSTMENT("Điều chỉnh tiền lương và phụ cấp"),
    POSITION_TRANSFER("Bổ nhiệm chức vụ / Điều chuyển đơn vị công tác"),
    TERM_MODIFICATION("Sửa đổi thời hạn hợp đồng"),
    WORKING_CONDITION("Thay đổi chế độ & điều kiện làm việc"),
    OTHER_TERMS("Điều khoản bổ sung khác (NDA, đào tạo...)");

    private final String description;
}

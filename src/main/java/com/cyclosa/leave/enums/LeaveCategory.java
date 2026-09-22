package com.cyclosa.leave.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 4 nhóm bản chất pháp lý của loại ngày nghỉ theo BLLĐ 2019 và Luật BHXH.
 */
@Getter
@RequiredArgsConstructor
public enum LeaveCategory {
    ANNUAL("Nghỉ hàng năm (Phép năm - Điều 113)"),
    PUBLIC_HOLIDAY("Nghỉ Lễ, Tết (11 ngày hưởng nguyên lương - Điều 112)"),
    PERSONAL_PAID("Nghỉ việc riêng hưởng nguyên lương (Điều 115.1)"),
    PERSONAL_UNPAID("Nghỉ việc riêng không hưởng lương (Điều 115.2, 115.3)"),
    SICK("Nghỉ ốm đau (Hưởng chế độ BHXH)"),
    MATERNITY("Nghỉ thai sản (Hưởng chế độ BHXH)");

    private final String description;
}

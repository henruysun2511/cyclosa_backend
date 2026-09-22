package com.cyclosa.leave.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Điều kiện làm việc theo quy định tại Điều 113 Bộ luật Lao động 2019.
 */
@Getter
@RequiredArgsConstructor
public enum JobConditionLevel {
    NORMAL("Điều kiện bình thường", BigDecimal.valueOf(12)),
    HAZARDOUS("Nặng nhọc, độc hại, nguy hiểm", BigDecimal.valueOf(14)),
    SPECIAL_HAZARDOUS("Đặc biệt nặng nhọc, độc hại, nguy hiểm", BigDecimal.valueOf(16));

    private final String description;
    private final BigDecimal defaultAnnualLeaveDays;
}

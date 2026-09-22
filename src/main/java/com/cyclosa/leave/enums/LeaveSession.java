package com.cyclosa.leave.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Buổi xin nghỉ phép trong ngày.
 */
@Getter
@RequiredArgsConstructor
public enum LeaveSession {
    FULL_DAY("Cả ngày", BigDecimal.valueOf(1.0)),
    MORNING("Buổi sáng (0.5 ngày)", BigDecimal.valueOf(0.5)),
    AFTERNOON("Buổi chiều (0.5 ngày)", BigDecimal.valueOf(0.5));

    private final String description;
    private final BigDecimal dayFraction;
}

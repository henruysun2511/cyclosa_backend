package com.cyclosa.attendance.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái đơn giải trình chấm công")
public enum ExplanationStatus {
    PENDING,
    APPROVED,
    REJECTED
}

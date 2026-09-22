package com.cyclosa.attendance.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lý do gửi đơn giải trình chấm công")
public enum ExplanationReasonType {
    FORGOT_CHECK_IN,
    FORGOT_CHECK_OUT,
    BUSINESS_TRIP,
    DEVICE_ERROR,
    OTHER
}

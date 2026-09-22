package com.cyclosa.attendance.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái điểm danh hàng ngày")
public enum AttendanceStatus {
    ON_TIME,
    LATE,
    EARLY,
    LATE_AND_EARLY,
    MISSING_CHECK_OUT,
    ABSENT,
    EXPLAINED
}

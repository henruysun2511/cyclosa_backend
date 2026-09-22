package com.cyclosa.attendance.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái phân ca làm việc")
public enum ShiftAssignmentStatus {
    ASSIGNED,
    CANCELLED,
    SWAPPED
}

package com.cyclosa.attendance.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Chế độ làm việc")
public enum WorkMode {
    ONSITE,
    REMOTE,
    HYBRID
}

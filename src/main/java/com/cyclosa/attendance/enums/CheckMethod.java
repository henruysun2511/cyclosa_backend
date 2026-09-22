package com.cyclosa.attendance.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phương thức chấm công điểm danh")
public enum CheckMethod {
    GPS,
    MOBILE_GPS,
    WIFI_IP,
    QR_CODE,
    MANUAL_ADMIN,
    MANUAL,
    FACE_RECOGNITION,
    FINGERPRINT
}

package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobPositionStatus {
    DRAFT("Bản nháp"),
    ACTIVE("Đang mở tuyển"),
    INACTIVE("Tạm dừng"),
    CLOSED("Đã đóng");

    private final String description;
}

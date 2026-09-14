package com.cyclosa.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Đang hoạt động"),
    LOCKED("Tạm khóa"),
    DISABLED("Vô hiệu hóa");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}

package com.cyclosa.employee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaritalStatus {
    SINGLE("Độc thân"),
    MARRIED("Đã kết hôn"),
    DIVORCED("Đã ly hôn"),
    WIDOWED("Góa bụa");

    private final String description;
}

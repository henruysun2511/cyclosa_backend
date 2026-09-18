package com.cyclosa.employee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentType {
    FULL_TIME("Toàn thời gian"),
    PART_TIME("Bán thời gian"),
    INTERNSHIP("Thực tập sinh"),
    CONTRACTOR("Cộng tác viên / Thời vụ");

    private final String description;
}

package com.cyclosa.employee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentStatus {
    PROBATION("Thử việc"),
    ACTIVE("Chính thức"),
    ON_LEAVE("Nghỉ phép / Thai sản"),
    RESIGNED("Đã thôi việc"),
    TERMINATED("Bị sa thải / Đã nghỉ việc");

    private final String description;
}

package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewStatus {
    SCHEDULED("Đã lên lịch"),
    COMPLETED("Đã hoàn thành"),
    RESCHEDULED("Đã dời lịch"),
    CANCELLED("Đã hủy");

    private final String description;
}

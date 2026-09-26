package com.cyclosa.onboarding.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingStatus {
    IN_PROGRESS("Đang thực hiện"),
    COMPLETED("Hoàn tất"),
    CANCELLED("Đã hủy");

    private final String description;
}

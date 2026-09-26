package com.cyclosa.onboarding.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingItemStatus {
    PENDING("Chờ thực hiện"),
    COMPLETED("Đã hoàn thành"),
    SKIPPED("Bỏ qua");

    private final String description;
}

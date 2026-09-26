package com.cyclosa.onboarding.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingItemCategory {
    DOCUMENT("Hồ sơ & Tài liệu"),
    EQUIPMENT("Trang thiết bị & Tài sản"),
    ACCOUNT("Tài khoản & Phân quyền hệ thống"),
    TRAINING("Đào tạo hội nhập & Định hướng"),
    OTHER("Hạng mục khác");

    private final String description;
}

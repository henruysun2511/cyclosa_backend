package com.cyclosa.onboarding.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProvisioningStatus {
    PENDING("Chờ cấp phát"),
    PROVISIONED("Đã cấp phát"),
    REVOKED("Đã thu hồi");

    private final String description;
}

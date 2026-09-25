package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManpowerRequestStatus {
    DRAFT("Bản nháp"),
    PENDING_APPROVAL("Chờ phê duyệt"),
    APPROVED("Đã phê duyệt"),
    REJECTED("Bị từ chối"),
    CANCELLED("Đã hủy"),
    FULFILLED("Đã tuyển đủ");

    private final String description;
}

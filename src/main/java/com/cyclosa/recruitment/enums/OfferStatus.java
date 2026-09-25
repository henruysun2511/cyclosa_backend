package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OfferStatus {
    DRAFT("Bản nháp"),
    SENT("Đã gửi đến ứng viên"),
    ACCEPTED("Ứng viên đã chấp nhận"),
    DECLINED("Ứng viên từ chối"),
    EXPIRED("Hết hạn"),
    CANCELLED("Đã thu hồi / Hủy");

    private final String description;
}

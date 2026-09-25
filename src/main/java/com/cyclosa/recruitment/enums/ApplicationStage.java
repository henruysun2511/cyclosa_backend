package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStage {
    APPLIED("Đã nộp hồ sơ"),
    SCREENING("Sàng lọc hồ sơ"),
    INTERVIEWING("Đang phỏng vấn"),
    OFFER_SENT("Đã gửi Offer"),
    OFFER_ACCEPTED("Đã chấp nhận Offer"),
    OFFER_DECLINED("Đã từ chối Offer"),
    HIRED("Đã tiếp nhận (Hired)"),
    REJECTED("Không đạt");

    private final String description;
}

package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostingChannel {
    WEBSITE("Trang tuyển dụng công ty"),
    JOB_BOARD("Trang việc làm trực tuyến"),
    SOCIAL_MEDIA("Mạng xã hội"),
    INTERNAL_REFERRAL("Giới thiệu nội bộ"),
    OTHER("Kênh khác");

    private final String description;
}

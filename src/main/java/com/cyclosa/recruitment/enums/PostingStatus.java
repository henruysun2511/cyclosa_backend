package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostingStatus {
    DRAFT("Bản nháp"),
    PUBLISHED("Đã xuất bản"),
    EXPIRED("Đã hết hạn"),
    CLOSED("Đã đóng");

    private final String description;
}

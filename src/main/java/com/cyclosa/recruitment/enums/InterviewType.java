package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewType {
    PHONE_SCREEN("Sàng lọc điện thoại"),
    TECHNICAL("Phỏng vấn chuyên môn"),
    CULTURE_FIT("Đánh giá văn hóa"),
    FINAL_ROUND("Phỏng vấn vòng cuối");

    private final String description;
}

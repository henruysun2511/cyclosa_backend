package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CandidateStatus {
    ACTIVE("Đang hoạt động"),
    BLACKLISTED("Trong danh sách đen"),
    HIRED("Đã tuyển dụng");

    private final String description;
}

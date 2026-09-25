package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CandidateSource {
    CAREER_PAGE("Trang tuyển dụng"),
    LINKEDIN("LinkedIn"),
    TOPCV("TopCV"),
    VIETNAMWORKS("VietnamWorks"),
    REFERRAL("Giới thiệu"),
    HEADHUNTER("Headhunter"),
    OTHER("Nguồn khác");

    private final String description;
}

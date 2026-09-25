package com.cyclosa.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewRecommendation {
    STRONG_YES("Rất phù hợp (Strong Yes)"),
    YES("Phù hợp (Yes)"),
    NO("Không phù hợp (No)"),
    STRONG_NO("Hoàn toàn không phù hợp (Strong No)");

    private final String description;
}

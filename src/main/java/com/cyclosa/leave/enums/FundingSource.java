package com.cyclosa.leave.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Nguồn kinh phí chi trả cho ngày nghỉ phép.
 */
@Getter
@RequiredArgsConstructor
public enum FundingSource {
    COMPANY("Doanh nghiệp chi trả nguyên lương"),
    SOCIAL_INSURANCE_FUND("Quỹ Bảo hiểm Xã hội chi trả trợ cấp");

    private final String description;
}

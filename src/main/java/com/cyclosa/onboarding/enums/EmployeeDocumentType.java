package com.cyclosa.onboarding.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeDocumentType {
    ID_CARD("CMND / Căn cước công dân"),
    DEGREE("Bằng cấp đại học / cao đẳng"),
    CERTIFICATE("Chứng chỉ chuyên môn"),
    CONTRACT("Hợp đồng lao động / Phụ lục"),
    TAX_CODE("Mã số thuế / Giấy tờ giảm trừ"),
    RESUME("CV / Sơ yếu lý lịch"),
    OTHER("Tài liệu khác");

    private final String description;
}

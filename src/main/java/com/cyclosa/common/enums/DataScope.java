package com.cyclosa.common.enums;

import lombok.Getter;

@Getter
public enum DataScope {
    OWN(1, "Chỉ dữ liệu của bản thân (Self)"),
    TEAM(2, "Dữ liệu trong phạm vi nhóm (Team)"),
    DEPARTMENT(3, "Dữ liệu trong phạm vi phòng ban (Department)"),
    COMPANY(4, "Dữ liệu trong toàn công ty/chi nhánh (Company)"),
    ALL(5, "Toàn bộ hệ thống đa công ty (All)");

    private final int level;
    private final String description;

    DataScope(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public boolean isBroaderOrEqualTo(DataScope other) {
        if (other == null) return true;
        return this.level >= other.level;
    }

    public static DataScope max(DataScope s1, DataScope s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        return s1.level >= s2.level ? s1 : s2;
    }
}

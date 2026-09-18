package com.cyclosa.contract.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Chế độ thời giờ làm việc theo Điều 105 BLLĐ 2019.
 */
@Getter
@RequiredArgsConstructor
public enum WorkingHoursType {
    STANDARD_44H("Tiêu chuẩn 44 giờ/tuần (Thứ 2 đến trưa Thứ 7)"),
    STANDARD_48H("Tiêu chuẩn 48 giờ/tuần (Thứ 2 đến hết Thứ 7)"),
    STANDARD_40H("Tiêu chuẩn 40 giờ/tuần (Thứ 2 đến hết Thứ 6)"),
    PART_TIME("Làm việc bán thời gian"),
    ROTATING_SHIFT("Làm việc theo ca xoay vòng"),
    FLEXIBLE("Thời giờ làm việc linh hoạt");

    private final String description;
}

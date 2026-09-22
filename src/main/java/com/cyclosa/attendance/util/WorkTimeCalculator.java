package com.cyclosa.attendance.util;

import com.cyclosa.attendance.enums.AttendanceStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Tiện ích tính toán thời gian làm việc, đi muộn, về sớm và quy đổi ngày công
 * theo chuẩn Bộ luật Lao động 2019 & Nghị định 145/2020/NĐ-CP.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkTimeCalculator {

    /**
     * Tính số phút đi muộn (đã trừ thời gian ân hạn).
     *
     * @param checkInTime      Giờ check-in thực tế
     * @param shiftStartTime   Giờ bắt đầu ca
     * @param graceLateMinutes Số phút ân hạn cho phép
     * @return Số phút đi muộn (>= 0)
     */
    public static int calculateLateMinutes(LocalTime checkInTime, LocalTime shiftStartTime, Integer graceLateMinutes) {
        if (checkInTime == null || shiftStartTime == null) {
            return 0;
        }
        int grace = graceLateMinutes != null ? Math.max(0, graceLateMinutes) : 0;
        LocalTime allowedLatest = shiftStartTime.plusMinutes(grace);

        if (!checkInTime.isAfter(allowedLatest)) {
            return 0;
        }

        long diffMinutes = Duration.between(shiftStartTime, checkInTime).toMinutes();
        return (int) Math.max(0, diffMinutes - grace);
    }

    /**
     * Tính số phút về sớm (đã trừ thời gian ân hạn).
     *
     * @param checkOutTime      Giờ check-out thực tế
     * @param shiftEndTime      Giờ kết thúc ca
     * @param graceEarlyMinutes Số phút ân hạn về sớm
     * @return Số phút về sớm (>= 0)
     */
    public static int calculateEarlyMinutes(LocalTime checkOutTime, LocalTime shiftEndTime, Integer graceEarlyMinutes) {
        if (checkOutTime == null || shiftEndTime == null) {
            return 0;
        }
        int grace = graceEarlyMinutes != null ? Math.max(0, graceEarlyMinutes) : 0;
        LocalTime allowedEarliest = shiftEndTime.minusMinutes(grace);

        if (!checkOutTime.isBefore(allowedEarliest)) {
            return 0;
        }

        long diffMinutes = Duration.between(checkOutTime, shiftEndTime).toMinutes();
        return (int) Math.max(0, diffMinutes - grace);
    }

    /**
     * Tính số giờ làm việc thực tế trong ngày, tự động trừ thời gian nghỉ giữa ca nếu có.
     *
     * @param checkInTime     Thời điểm check-in
     * @param checkOutTime    Thời điểm check-out
     * @param breakStartTime  Giờ bắt đầu nghỉ ca (nullable)
     * @param breakEndTime    Giờ kết thúc nghỉ ca (nullable)
     * @return Số giờ làm việc thực tế (BigDecimal 2 chữ số thập phân)
     */
    public static BigDecimal calculateActualHours(
            LocalDateTime checkInTime,
            LocalDateTime checkOutTime,
            LocalTime breakStartTime,
            LocalTime breakEndTime
    ) {
        if (checkInTime == null || checkOutTime == null || checkOutTime.isBefore(checkInTime)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long totalMinutes = Duration.between(checkInTime, checkOutTime).toMinutes();

        // Trừ thời gian nghỉ trưa nếu ca có cấu hình và thời gian làm việc bao phủ
        if (breakStartTime != null && breakEndTime != null && breakEndTime.isAfter(breakStartTime)) {
            LocalTime checkInLocalTime = checkInTime.toLocalTime();
            LocalTime checkOutLocalTime = checkOutTime.toLocalTime();

            // Tính phần giao nhau giữa [checkInLocalTime, checkOutLocalTime] và [breakStartTime, breakEndTime]
            LocalTime overlapStart = checkInLocalTime.isAfter(breakStartTime) ? checkInLocalTime : breakStartTime;
            LocalTime overlapEnd = checkOutLocalTime.isBefore(breakEndTime) ? checkOutLocalTime : breakEndTime;

            if (overlapEnd.isAfter(overlapStart)) {
                long breakMinutes = Duration.between(overlapStart, overlapEnd).toMinutes();
                totalMinutes = Math.max(0, totalMinutes - breakMinutes);
            }
        }

        double hours = totalMinutes / 60.0;
        return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Quy đổi số công thực tế theo chuẩn BLLĐ 2019 (Mục 3.3 BA):
     * - Làm đủ ca hoặc về sớm <= 15 phút (thực tế >= 7.5 giờ) -> 1.0 công
     * - Làm từ 4.0 giờ đến dưới 7.5 giờ -> 0.5 công
     * - Làm dưới 4.0 giờ -> 0.0 công
     *
     * @param actualHours      Số giờ làm việc thực tế
     * @param standardWorkUnits Mức công chuẩn của ca (mặc định 1.00)
     * @return Số công thực tế được hưởng
     */
    public static BigDecimal calculateActualWorkUnits(BigDecimal actualHours, BigDecimal standardWorkUnits) {
        if (actualHours == null || actualHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal baseUnits = standardWorkUnits != null ? standardWorkUnits : BigDecimal.ONE;

        if (actualHours.compareTo(BigDecimal.valueOf(7.5)) >= 0) {
            return baseUnits.setScale(2, RoundingMode.HALF_UP);
        } else if (actualHours.compareTo(BigDecimal.valueOf(4.0)) >= 0) {
            return baseUnits.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Xác định trạng thái điểm danh trong ngày.
     *
     * @param hasCheckedIn  Đã check-in chưa
     * @param hasCheckedOut Đã check-out chưa
     * @param lateMinutes   Số phút đi muộn
     * @param earlyMinutes  Số phút về sớm
     * @return AttendanceStatus tương ứng
     */
    public static AttendanceStatus resolveAttendanceStatus(
            boolean hasCheckedIn,
            boolean hasCheckedOut,
            int lateMinutes,
            int earlyMinutes
    ) {
        if (!hasCheckedIn) {
            return AttendanceStatus.ABSENT;
        }
        if (!hasCheckedOut) {
            return AttendanceStatus.MISSING_CHECK_OUT;
        }
        boolean isLate = lateMinutes > 0;
        boolean isEarly = earlyMinutes > 0;

        if (isLate && isEarly) {
            return AttendanceStatus.LATE_AND_EARLY;
        }
        if (isLate) {
            return AttendanceStatus.LATE;
        }
        if (isEarly) {
            return AttendanceStatus.EARLY;
        }
        return AttendanceStatus.ON_TIME;
    }
}

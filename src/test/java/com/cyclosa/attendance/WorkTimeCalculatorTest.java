package com.cyclosa.attendance;

import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.util.WorkTimeCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkTimeCalculatorTest {

    @Test
    @DisplayName("Tính phút đi muộn với thời gian ân hạn")
    void testCalculateLateMinutes() {
        LocalTime shiftStart = LocalTime.of(8, 0);

        // Đến sớm hơn giờ bắt đầu -> 0 phút
        assertEquals(0, WorkTimeCalculator.calculateLateMinutes(LocalTime.of(7, 50), shiftStart, 15));

        // Đến đúng giờ -> 0 phút
        assertEquals(0, WorkTimeCalculator.calculateLateMinutes(LocalTime.of(8, 0), shiftStart, 15));

        // Đến trong khoảng ân hạn 15 phút (8:10) -> 0 phút
        assertEquals(0, WorkTimeCalculator.calculateLateMinutes(LocalTime.of(8, 10), shiftStart, 15));

        // Đến đúng 8:15 (mốc ân hạn) -> 0 phút
        assertEquals(0, WorkTimeCalculator.calculateLateMinutes(LocalTime.of(8, 15), shiftStart, 15));

        // Đến 8:30 (muộn 30 phút, trừ 15 phút ân hạn = 15 phút phạt)
        assertEquals(15, WorkTimeCalculator.calculateLateMinutes(LocalTime.of(8, 30), shiftStart, 15));
    }

    @Test
    @DisplayName("Tính phút về sớm với thời gian ân hạn")
    void testCalculateEarlyMinutes() {
        LocalTime shiftEnd = LocalTime.of(17, 30);

        // Về sau giờ ca -> 0 phút
        assertEquals(0, WorkTimeCalculator.calculateEarlyMinutes(LocalTime.of(17, 45), shiftEnd, 10));

        // Về đúng giờ ca -> 0 phút
        assertEquals(0, WorkTimeCalculator.calculateEarlyMinutes(LocalTime.of(17, 30), shiftEnd, 10));

        // Về lúc 17:25 (trong ân hạn 10 phút) -> 0 phút
        assertEquals(0, WorkTimeCalculator.calculateEarlyMinutes(LocalTime.of(17, 25), shiftEnd, 10));

        // Về lúc 17:00 (sớm 30 phút, trừ 10 phút ân hạn = 20 phút phạt)
        assertEquals(20, WorkTimeCalculator.calculateEarlyMinutes(LocalTime.of(17, 0), shiftEnd, 10));
    }

    @Test
    @DisplayName("Tính giờ làm việc thực tế và khấu trừ giờ nghỉ trưa")
    void testCalculateActualHoursWithBreak() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 10, 1, 8, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 10, 1, 17, 30); // 9.5 tiếng
        LocalTime breakStart = LocalTime.of(12, 0);
        LocalTime breakEnd = LocalTime.of(13, 30); // Nghỉ 1.5 tiếng

        // 9.5h - 1.5h = 8.00h
        BigDecimal actualHours = WorkTimeCalculator.calculateActualHours(checkIn, checkOut, breakStart, breakEnd);
        assertEquals(new BigDecimal("8.00"), actualHours);
    }

    @Test
    @DisplayName("Quy đổi công theo chuẩn BLLĐ 2019")
    void testCalculateActualWorkUnits() {
        // >= 7.5h -> 1.00 công
        assertEquals(new BigDecimal("1.00"), WorkTimeCalculator.calculateActualWorkUnits(new BigDecimal("8.00"), BigDecimal.ONE));
        assertEquals(new BigDecimal("1.00"), WorkTimeCalculator.calculateActualWorkUnits(new BigDecimal("7.50"), BigDecimal.ONE));

        // 4.0h <= T < 7.5h -> 0.50 công
        assertEquals(new BigDecimal("0.50"), WorkTimeCalculator.calculateActualWorkUnits(new BigDecimal("6.00"), BigDecimal.ONE));
        assertEquals(new BigDecimal("0.50"), WorkTimeCalculator.calculateActualWorkUnits(new BigDecimal("4.00"), BigDecimal.ONE));

        // < 4.0h -> 0.00 công
        assertEquals(new BigDecimal("0.00"), WorkTimeCalculator.calculateActualWorkUnits(new BigDecimal("3.50"), BigDecimal.ONE));
        assertEquals(new BigDecimal("0.00"), WorkTimeCalculator.calculateActualWorkUnits(BigDecimal.ZERO, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Xác định trạng thái điểm danh")
    void testResolveAttendanceStatus() {
        // Chưa check-in -> ABSENT
        assertEquals(AttendanceStatus.ABSENT, WorkTimeCalculator.resolveAttendanceStatus(false, false, 0, 0));

        // Đã check-in nhưng chưa check-out -> MISSING_CHECK_OUT
        assertEquals(AttendanceStatus.MISSING_CHECK_OUT, WorkTimeCalculator.resolveAttendanceStatus(true, false, 0, 0));

        // Đúng giờ -> ON_TIME
        assertEquals(AttendanceStatus.ON_TIME, WorkTimeCalculator.resolveAttendanceStatus(true, true, 0, 0));

        // Đi muộn -> LATE
        assertEquals(AttendanceStatus.LATE, WorkTimeCalculator.resolveAttendanceStatus(true, true, 10, 0));

        // Về sớm -> EARLY
        assertEquals(AttendanceStatus.EARLY, WorkTimeCalculator.resolveAttendanceStatus(true, true, 0, 15));

        // Cả đi muộn và về sớm -> LATE_AND_EARLY
        assertEquals(AttendanceStatus.LATE_AND_EARLY, WorkTimeCalculator.resolveAttendanceStatus(true, true, 10, 15));
    }
}

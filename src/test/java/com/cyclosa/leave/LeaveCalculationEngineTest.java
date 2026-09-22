package com.cyclosa.leave;

import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.JobConditionLevel;
import com.cyclosa.leave.enums.LeaveCategory;
import com.cyclosa.leave.enums.LeaveSession;
import com.cyclosa.leave.service.LeaveCalculationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveCalculationEngineTest {

    private LeaveCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new LeaveCalculationEngine();
    }

    @Test
    @DisplayName("Tính ngày nghỉ thường: loại trừ Thứ 7, Chủ Nhật")
    void testCalculateLeaveDays_excludesWeekend() {
        LeaveType annualLeave = LeaveType.builder()
                .category(LeaveCategory.ANNUAL)
                .build();

        // 2026-10-01 (Thứ 5) đến 2026-10-06 (Thứ 3)
        // Gồm: T5 (1/10), T6 (2/10), T7 (3/10 - skip), CN (4/10 - skip), T2 (5/10), T3 (6/10) = 4 ngày làm việc
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 6);

        BigDecimal days = engine.calculateLeaveDays(annualLeave, start, end, LeaveSession.FULL_DAY);
        assertThat(days).isEqualByComparingTo(BigDecimal.valueOf(4.0));
    }

    @Test
    @DisplayName("Tính ngày nghỉ việc riêng có số ngày cố định theo Điều 115 (Kết hôn 3 ngày)")
    void testCalculateLeaveDays_marriageFixedDays() {
        LeaveType marriageLeave = LeaveType.builder()
                .category(LeaveCategory.PERSONAL_PAID)
                .fixedDaysPerEvent(BigDecimal.valueOf(3.0))
                .build();

        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 1);

        BigDecimal days = engine.calculateLeaveDays(marriageLeave, start, end, LeaveSession.FULL_DAY);
        assertThat(days).isEqualByComparingTo(BigDecimal.valueOf(3.0));
    }

    @Test
    @DisplayName("Tính ngày nghỉ nửa ngày (Morning/Afternoon): 0.5 ngày")
    void testCalculateLeaveDays_halfDaySession() {
        LeaveType annualLeave = LeaveType.builder()
                .category(LeaveCategory.ANNUAL)
                .build();

        // 2026-10-02 là Thứ 6
        LocalDate date = LocalDate.of(2026, 10, 2);

        BigDecimal morningDays = engine.calculateLeaveDays(annualLeave, date, date, LeaveSession.MORNING);
        assertThat(morningDays).isEqualByComparingTo(BigDecimal.valueOf(0.5));

        BigDecimal afternoonDays = engine.calculateLeaveDays(annualLeave, date, date, LeaveSession.AFTERNOON);
        assertThat(afternoonDays).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    @DisplayName("Tính thâm niên Điều 114: Cứ đủ 5 năm thâm niên cộng 1 ngày phép")
    void testCalculateAnnualLeaveEntitlement_seniorityBonus() {
        // Vào làm từ 2016-01-01, tính cho năm 2026 (10 năm thâm niên -> +2 ngày)
        LocalDate hireDate = LocalDate.of(2016, 1, 1);
        int targetYear = 2026;

        BigDecimal entitlement = engine.calculateAnnualLeaveEntitlement(
                hireDate, targetYear, JobConditionLevel.NORMAL, BigDecimal.valueOf(12.0), 5, BigDecimal.valueOf(1.0)
        );

        // 12 ngày cơ bản + 2 ngày thâm niên (10/5 = 2) = 14 ngày
        assertThat(entitlement).isEqualByComparingTo(BigDecimal.valueOf(14.0));
    }

    @Test
    @DisplayName("Tính tỷ lệ phép năm cho nhân viên tuyển mới giữa năm (Điều 113.2)")
    void testCalculateAnnualLeaveEntitlement_midYearHire() {
        // Tuyển vào ngày 2026-07-01 (làm việc 6 tháng trong năm 2026)
        LocalDate hireDate = LocalDate.of(2026, 7, 1);
        int targetYear = 2026;

        BigDecimal entitlement = engine.calculateAnnualLeaveEntitlement(
                hireDate, targetYear, JobConditionLevel.NORMAL, BigDecimal.valueOf(12.0), 5, BigDecimal.valueOf(1.0)
        );

        // (6 / 12) * 12 = 6 ngày
        assertThat(entitlement).isEqualByComparingTo(BigDecimal.valueOf(6.0));
    }

    @Test
    @DisplayName("Tính tiền thanh toán ngày phép năm chưa nghỉ Điều 113.3")
    void testCalculateUnusedLeavePayout() {
        BigDecimal basicSalary = BigDecimal.valueOf(22000000); // 22 triệu
        BigDecimal standardDays = BigDecimal.valueOf(22);     // 22 ngày tiêu chuẩn (1tr/ngày)
        BigDecimal remainingDays = BigDecimal.valueOf(3.5);   // Còn 3.5 ngày phép

        BigDecimal payout = engine.calculateUnusedLeavePayout(basicSalary, standardDays, remainingDays);
        // 1.000.000 * 3.5 = 3.500.000 VNĐ
        assertThat(payout).isEqualByComparingTo(BigDecimal.valueOf(3500000.00));
    }
}

package com.cyclosa.leave.service;

import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.enums.JobConditionLevel;
import com.cyclosa.leave.enums.LeaveCategory;
import com.cyclosa.leave.enums.LeaveSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Bộ máy tính toán ngày nghỉ và quỹ phép độc lập (Leave Calculation Engine),
 * tuân thủ chặt chẽ Bộ luật Lao động 2019 và Luật BHXH.
 */
@Slf4j
@Component
public class LeaveCalculationEngine {

    // Danh sách ngày nghỉ Lễ, Tết cố định theo Dương lịch tại Việt Nam (Điều 112 BLLĐ)
    private static final Set<MonthDay> FIXED_PUBLIC_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),   // Tết Dương lịch
            MonthDay.of(4, 30),  // Ngày Chiến thắng
            MonthDay.of(5, 1),   // Ngày Quốc tế Lao động
            MonthDay.of(9, 1),   // Ngày Quốc khánh (liền kề)
            MonthDay.of(9, 2)    // Ngày Quốc khánh chính thức
    );

    /**
     * Tính số ngày công thực trừ của một đơn xin nghỉ phép.
     * - Với nhóm việc riêng (Điều 115): Tự động trả về số ngày cố định theo sự kiện (fixedDaysPerEvent).
     * - Với các nhóm khác: Đếm số ngày thực tế trong khoảng, loại trừ Thứ Bảy, Chủ Nhật và ngày Lễ Tết.
     * - Với nghỉ nửa ngày (Morning/Afternoon): Tính 0.5 ngày.
     */
    public BigDecimal calculateLeaveDays(LeaveType leaveType, LocalDate startDate, LocalDate endDate, LeaveSession session) {
        if (leaveType == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }

        // Nhóm việc riêng (Điều 115) có số ngày cố định quy định sẵn theo sự kiện
        if ((leaveType.getCategory() == LeaveCategory.PERSONAL_PAID || leaveType.getCategory() == LeaveCategory.PERSONAL_UNPAID)
                && leaveType.getFixedDaysPerEvent() != null && leaveType.getFixedDaysPerEvent().compareTo(BigDecimal.ZERO) > 0) {
            return leaveType.getFixedDaysPerEvent();
        }

        // Nếu xin nghỉ trong cùng 1 ngày và chọn buổi sáng/chiều
        if (startDate.isEqual(endDate) && (session == LeaveSession.MORNING || session == LeaveSession.AFTERNOON)) {
            // Nếu ngày đó là cuối tuần hoặc lễ tết thì không tính
            if (isWeekend(startDate) || isPublicHoliday(startDate)) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(0.5);
        }

        // Đếm số ngày làm việc thực tế giữa startDate và endDate
        int workingDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!isWeekend(current) && !isPublicHoliday(current)) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return BigDecimal.valueOf(workingDays).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Tính toán tổng số ngày phép năm được hưởng của nhân sự trong năm (Điều 113, Điều 114 BLLĐ 2019):
     * 1. Định mức cơ bản theo điều kiện làm việc: 12 / 14 / 16 ngày.
     * 2. Tỷ lệ tháng làm việc nếu tuyển mới giữa năm (chưa đủ 12 tháng).
     * 3. Thâm niên làm việc (Điều 114): Cứ đủ 5 năm làm việc liên tục cộng 1 ngày phép.
     */
    public BigDecimal calculateAnnualLeaveEntitlement(
            LocalDate hireDate,
            int targetYear,
            JobConditionLevel conditionLevel,
            BigDecimal customBaseDays,
            int seniorityBonusYears,
            BigDecimal seniorityBonusDays
    ) {
        if (hireDate == null) {
            return BigDecimal.valueOf(12.0);
        }

        // 1. Xác định mức phép năm cơ bản
        BigDecimal baseDays = customBaseDays != null
                ? customBaseDays
                : (conditionLevel != null ? conditionLevel.getDefaultAnnualLeaveDays() : BigDecimal.valueOf(12.0));

        // 2. Tính tỷ lệ nếu nhân viên vào làm trong chính targetYear (chưa đủ 12 tháng)
        BigDecimal entitledBaseDays;
        int hireYear = hireDate.getYear();
        if (hireYear == targetYear) {
            // Số tháng làm việc trong năm: từ tháng vào làm đến hết tháng 12
            int monthsWorked = 12 - hireDate.getMonthValue() + 1;
            // Nếu vào làm sau ngày 15 của tháng thì tháng đó có thể tính nửa tháng
            if (hireDate.getDayOfMonth() > 15) {
                monthsWorked = Math.max(0, monthsWorked - 1);
            }
            entitledBaseDays = baseDays.multiply(BigDecimal.valueOf(monthsWorked))
                    .divide(BigDecimal.valueOf(12), 1, RoundingMode.HALF_UP);
        } else if (hireYear > targetYear) {
            return BigDecimal.ZERO; // Chưa vào làm
        } else {
            entitledBaseDays = baseDays;
        }

        // 3. Tính ngày nghỉ tăng thêm theo thâm niên làm việc (Điều 114)
        // Tính số năm thâm niên tính đến ngày cuối cùng của targetYear
        LocalDate endOfTargetYear = LocalDate.of(targetYear, 12, 31);
        long yearsOfService = ChronoUnit.YEARS.between(hireDate, endOfTargetYear);

        BigDecimal seniorityBonus = BigDecimal.ZERO;
        if (seniorityBonusYears > 0 && yearsOfService >= seniorityBonusYears) {
            long bonusMultiplier = yearsOfService / seniorityBonusYears;
            BigDecimal bonusPerStep = seniorityBonusDays != null ? seniorityBonusDays : BigDecimal.valueOf(1.0);
            seniorityBonus = bonusPerStep.multiply(BigDecimal.valueOf(bonusMultiplier));
        }

        return entitledBaseDays.add(seniorityBonus).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Tính toán số tiền thanh toán ngày phép năm chưa nghỉ khi thôi việc (Điều 113.3 BLLĐ 2019).
     * Tiền thanh toán = (Lương cơ bản / Số ngày làm việc tiêu chuẩn trong tháng) × Số ngày phép còn lại.
     */
    public BigDecimal calculateUnusedLeavePayout(BigDecimal basicSalary, BigDecimal standardWorkDays, BigDecimal remainingDays) {
        if (basicSalary == null || basicSalary.compareTo(BigDecimal.ZERO) <= 0
                || standardWorkDays == null || standardWorkDays.compareTo(BigDecimal.ZERO) <= 0
                || remainingDays == null || remainingDays.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRate = basicSalary.divide(standardWorkDays, 4, RoundingMode.HALF_UP);
        return dailyRate.multiply(remainingDays).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    public boolean isPublicHoliday(LocalDate date) {
        MonthDay md = MonthDay.from(date);
        return FIXED_PUBLIC_HOLIDAYS.contains(md);
    }
}

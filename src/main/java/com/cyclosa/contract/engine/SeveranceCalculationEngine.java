package com.cyclosa.contract.engine;

import com.cyclosa.contract.enums.TerminationGround;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * Engine tính toán Trợ cấp thôi việc (Điều 46) và Trợ cấp mất việc làm (Điều 47)
 * theo Bộ luật Lao động 2019 và Nghị định 145/2020/NĐ-CP.
 */
@Slf4j
@Component
public class SeveranceCalculationEngine {

    @Getter
    @Builder
    public static class SeveranceResult {
        private final BigDecimal severanceAllowance;   // Trợ cấp thôi việc (Điều 46)
        private final BigDecimal lossOfWorkAllowance; // Trợ cấp mất việc làm (Điều 47)
        private final double totalTenureYears;         // Tổng thời gian làm việc (năm)
        private final double bhtnYears;                // Thời gian đã đóng BHTN (năm)
        private final double qualifyingYears;          // Thời gian thực tế tính trợ cấp (đã làm tròn)
        private final BigDecimal averageSalary;        // Tiền lương bình quân 6 tháng liền kề
        private final boolean eligible;                // Đủ điều kiện hưởng
        private final String note;
    }

    /**
     * Tính toán trợ cấp theo quy định pháp luật.
     *
     * @param ground căn cứ chấm dứt hợp đồng
     * @param hireDate ngày bắt đầu làm việc tại công ty
     * @param finalWorkingDate ngày làm việc cuối cùng
     * @param monthsWithBhtn số tháng đã tham gia BHTN theo Luật Việc làm
     * @param average6MonthsSalary mức lương bình quân 6 tháng liền kề theo HĐLĐ
     */
    public SeveranceResult calculate(TerminationGround ground, LocalDate hireDate, LocalDate finalWorkingDate,
                                     int monthsWithBhtn, BigDecimal average6MonthsSalary) {

        if (hireDate == null || finalWorkingDate == null || !finalWorkingDate.isAfter(hireDate)) {
            return emptyResult(average6MonthsSalary, "Ngày làm việc không hợp lệ để tính trợ cấp");
        }

        Period totalPeriod = Period.between(hireDate, finalWorkingDate);
        long totalMonths = totalPeriod.toTotalMonths();

        // Điều 46.1 & 47.1: Phải làm việc thường xuyên từ đủ 12 tháng trở lên
        if (totalMonths < 12) {
            log.info("Thời gian làm việc {} tháng (< 12 tháng), không đủ điều kiện hưởng trợ cấp thôi việc/mất việc", totalMonths);
            return emptyResult(average6MonthsSalary, "Làm việc chưa đủ 12 tháng, không đủ điều kiện hưởng trợ cấp");
        }

        // Thời gian tính trợ cấp = Tổng thời gian làm việc - Thời gian đã tham gia BHTN
        long qualifyingMonths = Math.max(0, totalMonths - monthsWithBhtn);

        // Quy tắc làm tròn theo Nghị định 145/2020/NĐ-CP (Điều 8 khoản 3):
        // - Từ đủ 01 tháng đến dưới 06 tháng: tính bằng 1/2 năm (0.5 năm)
        // - Từ đủ 06 tháng trở lên: tính bằng 01 năm (1.0 năm)
        long fullYears = qualifyingMonths / 12;
        long remainderMonths = qualifyingMonths % 12;
        double roundedYears = fullYears;
        if (remainderMonths >= 1 && remainderMonths < 6) {
            roundedYears += 0.5;
        } else if (remainderMonths >= 6) {
            roundedYears += 1.0;
        }

        BigDecimal severance = BigDecimal.ZERO;
        BigDecimal jobLoss = BigDecimal.ZERO;

        BigDecimal safeAverageSalary = average6MonthsSalary != null ? average6MonthsSalary : BigDecimal.ZERO;

        if (ground.isEligibleForSeverance() && roundedYears > 0) {
            // Điều 46: Mỗi năm làm việc được trợ cấp 0.5 tháng tiền lương
            severance = safeAverageSalary.multiply(BigDecimal.valueOf(roundedYears * 0.5))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (ground.isEligibleForJobLoss() && roundedYears > 0) {
            // Điều 47: Mỗi năm làm việc được trợ cấp 01 tháng tiền lương; ít nhất bằng 02 tháng tiền lương
            BigDecimal calculatedJobLoss = safeAverageSalary.multiply(BigDecimal.valueOf(roundedYears))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal minJobLossFloor = safeAverageSalary.multiply(BigDecimal.valueOf(2.0))
                    .setScale(2, RoundingMode.HALF_UP);

            jobLoss = calculatedJobLoss.max(minJobLossFloor);
        }

        return SeveranceResult.builder()
                .severanceAllowance(severance)
                .lossOfWorkAllowance(jobLoss)
                .totalTenureYears(round2Decimals((double) totalMonths / 12.0))
                .bhtnYears(round2Decimals((double) monthsWithBhtn / 12.0))
                .qualifyingYears(roundedYears)
                .averageSalary(safeAverageSalary)
                .eligible(severance.compareTo(BigDecimal.ZERO) > 0 || jobLoss.compareTo(BigDecimal.ZERO) > 0)
                .note(String.format("Tổng công tác: %d tháng, đóng BHTN: %d tháng, thời gian tính: %.1f năm",
                        totalMonths, monthsWithBhtn, roundedYears))
                .build();
    }

    private SeveranceResult emptyResult(BigDecimal avgSalary, String note) {
        return SeveranceResult.builder()
                .severanceAllowance(BigDecimal.ZERO)
                .lossOfWorkAllowance(BigDecimal.ZERO)
                .totalTenureYears(0)
                .bhtnYears(0)
                .qualifyingYears(0)
                .averageSalary(avgSalary != null ? avgSalary : BigDecimal.ZERO)
                .eligible(false)
                .note(note)
                .build();
    }

    private double round2Decimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

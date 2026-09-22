package com.cyclosa.payroll.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bộ máy tính toán lương và nghĩa vụ pháp lý độc lập (Legal Compliance Calculation Engine).
 * Tuân thủ theo Bộ luật Lao động 2019, Luật Thuế TNCN và Luật BHXH.
 */
@Component
public class PayrollCalculationEngine {

    // Căn cứ pháp lý Nghị định 73/2024/NĐ-CP: Mức lương cơ sở 2.340.000 VNĐ
    public static final BigDecimal BASE_SALARY_LEVEL = BigDecimal.valueOf(2340000);
    // Trần đóng BHXH/BHYT: 20 lần lương cơ sở = 46.800.000 VNĐ
    public static final BigDecimal MAX_SOCIAL_INSURANCE_BASE = BASE_SALARY_LEVEL.multiply(BigDecimal.valueOf(20));

    // Căn cứ pháp lý Nghị định 74/2024/NĐ-CP: Lương tối thiểu Vùng 1 = 4.960.000 VNĐ
    public static final BigDecimal REGION_1_MIN_SALARY = BigDecimal.valueOf(4960000);
    // Trần đóng BHTN: 20 lần lương tối thiểu vùng = 99.200.000 VNĐ
    public static final BigDecimal MAX_UNEMPLOYMENT_INSURANCE_BASE = REGION_1_MIN_SALARY.multiply(BigDecimal.valueOf(20));

    // Tỷ lệ trích đóng Bảo hiểm của Người Lao Động (NLĐ)
    public static final BigDecimal RATE_BHXH_EMPLOYEE = BigDecimal.valueOf(0.08);   // 8.0%
    public static final BigDecimal RATE_BHYT_EMPLOYEE = BigDecimal.valueOf(0.015);  // 1.5%
    public static final BigDecimal RATE_BHTN_EMPLOYEE = BigDecimal.valueOf(0.01);   // 1.0%

    // Tỷ lệ trích nộp Bảo hiểm của Người Sử Dụng Lao Động (NSDLĐ)
    public static final BigDecimal RATE_BHXH_EMPLOYER = BigDecimal.valueOf(0.175);  // 17.5%
    public static final BigDecimal RATE_BHYT_EMPLOYER = BigDecimal.valueOf(0.03);   // 3.0%
    public static final BigDecimal RATE_BHTN_EMPLOYER = BigDecimal.valueOf(0.01);   // 1.0%

    // Giảm trừ gia cảnh (Nghị quyết 954/2020/UBTVQH14)
    public static final BigDecimal PERSONAL_RELIEF = BigDecimal.valueOf(11000000);        // 11.000.000 VNĐ/tháng
    public static final BigDecimal DEPENDENT_RELIEF_PER_PERSON = BigDecimal.valueOf(4400000); // 4.400.000 VNĐ/người/tháng

    // Miễn thuế ăn trưa tối đa (Thông tư 26/2016/TT-BLĐTBXH)
    public static final BigDecimal MAX_TAX_EXEMPT_LUNCH = BigDecimal.valueOf(730000);

    /**
     * Tính lương thời gian dựa trên số ngày công thực tế và ngày nghỉ phép hưởng nguyên lương.
     */
    public BigDecimal calculateTimeBasedSalary(BigDecimal basicSalary, BigDecimal standardWorkDays,
                                              BigDecimal actualWorkDays, BigDecimal paidLeaveDays) {
        if (basicSalary == null || basicSalary.compareTo(BigDecimal.ZERO) <= 0
                || standardWorkDays == null || standardWorkDays.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal actualDays = actualWorkDays != null ? actualWorkDays : BigDecimal.ZERO;
        BigDecimal leaveDays = paidLeaveDays != null ? paidLeaveDays : BigDecimal.ZERO;
        BigDecimal totalDays = actualDays.add(leaveDays);

        BigDecimal dailyRate = basicSalary.divide(standardWorkDays, 6, RoundingMode.HALF_UP);
        return dailyRate.multiply(totalDays).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tính tổng trích đóng bảo hiểm bắt buộc của Người lao động (10.5%).
     * Khống chế trần BHXH/BHYT (46.8tr) và trần BHTN (99.2tr).
     */
    public BigDecimal calculateSocialInsuranceEmployee(BigDecimal insuranceBaseSalary) {
        if (insuranceBaseSalary == null || insuranceBaseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal cappedSocialBase = insuranceBaseSalary.min(MAX_SOCIAL_INSURANCE_BASE);
        BigDecimal cappedUnempBase = insuranceBaseSalary.min(MAX_UNEMPLOYMENT_INSURANCE_BASE);

        BigDecimal bhxh = cappedSocialBase.multiply(RATE_BHXH_EMPLOYEE);
        BigDecimal bhyt = cappedSocialBase.multiply(RATE_BHYT_EMPLOYEE);
        BigDecimal bhtn = cappedUnempBase.multiply(RATE_BHTN_EMPLOYEE);

        return bhxh.add(bhyt).add(bhtn).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tính tổng trích đóng bảo hiểm bắt buộc của Doanh nghiệp (21.5%).
     */
    public BigDecimal calculateSocialInsuranceEmployer(BigDecimal insuranceBaseSalary) {
        if (insuranceBaseSalary == null || insuranceBaseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal cappedSocialBase = insuranceBaseSalary.min(MAX_SOCIAL_INSURANCE_BASE);
        BigDecimal cappedUnempBase = insuranceBaseSalary.min(MAX_UNEMPLOYMENT_INSURANCE_BASE);

        BigDecimal bhxh = cappedSocialBase.multiply(RATE_BHXH_EMPLOYER);
        BigDecimal bhyt = cappedSocialBase.multiply(RATE_BHYT_EMPLOYER);
        BigDecimal bhtn = cappedUnempBase.multiply(RATE_BHTN_EMPLOYER);

        return bhxh.add(bhyt).add(bhtn).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tính thuế TNCN theo biểu thuế lũy tiến từng phần 7 bậc chuẩn Luật Thuế TNCN Việt Nam.
     */
    public BigDecimal calculatePersonalIncomeTax(BigDecimal assessedIncome) {
        if (assessedIncome == null || assessedIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        double income = assessedIncome.doubleValue();
        double tax;

        if (income <= 5_000_000) {
            tax = income * 0.05;
        } else if (income <= 10_000_000) {
            tax = income * 0.10 - 250_000;
        } else if (income <= 18_000_000) {
            tax = income * 0.15 - 750_000;
        } else if (income <= 32_000_000) {
            tax = income * 0.20 - 1_650_000;
        } else if (income <= 52_000_000) {
            tax = income * 0.25 - 3_250_000;
        } else if (income <= 80_000_000) {
            tax = income * 0.30 - 5_850_000;
        } else {
            tax = income * 0.35 - 9_850_000;
        }

        return BigDecimal.valueOf(Math.max(0, tax)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tính lương thực nhận (Net Salary).
     */
    public BigDecimal calculateNetSalary(BigDecimal grossSalary, BigDecimal socialInsuranceEmployee,
                                         BigDecimal personalIncomeTax, BigDecimal advanceDeduction,
                                         BigDecimal otherDeductions) {
        BigDecimal gross = grossSalary != null ? grossSalary : BigDecimal.ZERO;
        BigDecimal insurance = socialInsuranceEmployee != null ? socialInsuranceEmployee : BigDecimal.ZERO;
        BigDecimal pit = personalIncomeTax != null ? personalIncomeTax : BigDecimal.ZERO;
        BigDecimal advance = advanceDeduction != null ? advanceDeduction : BigDecimal.ZERO;
        BigDecimal other = otherDeductions != null ? otherDeductions : BigDecimal.ZERO;

        BigDecimal net = gross.subtract(insurance).subtract(pit).subtract(advance).subtract(other);
        return net.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}

package com.cyclosa.payroll;

import com.cyclosa.payroll.service.PayrollCalculationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollCalculationEngineTest {

    private PayrollCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PayrollCalculationEngine();
    }

    @Test
    @DisplayName("Should calculate time-based salary correctly")
    void testCalculateTimeBasedSalary() {
        BigDecimal basicSalary = new BigDecimal("22000000");
        BigDecimal standardWorkDays = new BigDecimal("22");
        BigDecimal actualWorkDays = new BigDecimal("10");
        BigDecimal paidLeaveDays = new BigDecimal("1");

        BigDecimal result = engine.calculateTimeBasedSalary(
                basicSalary, standardWorkDays, actualWorkDays, paidLeaveDays
        );

        // 22,000,000 / 22 * 11 = 11,000,000
        assertThat(result).isEqualByComparingTo(new BigDecimal("11000000.00"));
    }

    @Test
    @DisplayName("Should compute employee and employer insurance under statutory cap")
    void testCalculateInsuranceUnderCap() {
        BigDecimal salary = new BigDecimal("20000000");

        BigDecimal employeeInsurance = engine.calculateSocialInsuranceEmployee(salary);
        // Employee: 20M * 10.5% (8% BHXH + 1.5% BHYT + 1% BHTN) = 2,100,000
        assertThat(employeeInsurance).isEqualByComparingTo(new BigDecimal("2100000.00"));

        BigDecimal employerInsurance = engine.calculateSocialInsuranceEmployer(salary);
        // Employer: 20M * 21.5% (17.5% BHXH + 3% BHYT + 1% BHTN) = 4,300,000
        assertThat(employerInsurance).isEqualByComparingTo(new BigDecimal("4300000.00"));
    }

    @Test
    @DisplayName("Should cap insurance at statutory ceiling for high salary")
    void testCalculateInsuranceCapping() {
        BigDecimal highSalary = new BigDecimal("120000000"); // 120M

        BigDecimal employeeInsurance = engine.calculateSocialInsuranceEmployee(highSalary);
        // Cap BHXH & BHYT: 20 * 2,340,000 = 46,800,000
        // Employee BHXH: 46.8M * 8% = 3,744,000
        // Employee BHYT: 46.8M * 1.5% = 702,000
        // Cap BHTN: 20 * 4,960,000 = 99,200,000
        // Employee BHTN: 99.2M * 1% = 992,000
        // Total Employee = 3,744,000 + 702,000 + 992,000 = 5,438,000
        assertThat(employeeInsurance).isEqualByComparingTo(new BigDecimal("5438000.00"));
    }

    @Test
    @DisplayName("Should calculate progressive Personal Income Tax across all 7 brackets")
    void testCalculatePersonalIncomeTax() {
        // Zero or negative
        assertThat(engine.calculatePersonalIncomeTax(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("-100"))).isEqualByComparingTo(BigDecimal.ZERO);

        // Bracket 1: <= 5M (5%)
        // 4,000,000 * 0.05 = 200,000
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("4000000")))
                .isEqualByComparingTo(new BigDecimal("200000.00"));

        // Bracket 2: 5M - 10M (10% - 250k)
        // 8,000,000 * 0.10 - 250,000 = 550,000
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("8000000")))
                .isEqualByComparingTo(new BigDecimal("550000.00"));

        // Bracket 3: 10M - 18M (15% - 750k)
        // 15,000,000 * 0.15 - 750,000 = 1,500,000
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("15000000")))
                .isEqualByComparingTo(new BigDecimal("1500000.00"));

        // Bracket 4: 18M - 32M (20% - 1.65M)
        // 25,000,000 * 0.20 - 1,650,000 = 3,350,000
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("25000000")))
                .isEqualByComparingTo(new BigDecimal("3350000.00"));

        // Bracket 5: 32M - 52M (25% - 3.25M)
        // 40,000,000 * 0.25 - 3,250,000 = 6,750,000
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("40000000")))
                .isEqualByComparingTo(new BigDecimal("6750000.00"));

        // Bracket 6: 52M - 80M (30% - 5.85M)
        // 60,000,000 * 0.30 - 5,850,000 = 12,150,000
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("60000000")))
                .isEqualByComparingTo(new BigDecimal("12150000.00"));

        // Bracket 7: > 80M (35% - 9.85M)
        // 100,000,000 * 0.35 - 9,850,000 = 25,150,000
        assertThat(engine.calculatePersonalIncomeTax(new BigDecimal("100000000")))
                .isEqualByComparingTo(new BigDecimal("25150000.00"));
    }

    @Test
    @DisplayName("Should calculate net salary correctly")
    void testCalculateNetSalary() {
        BigDecimal gross = new BigDecimal("25000000");
        BigDecimal employeeInsurance = new BigDecimal("2100000");
        BigDecimal pit = new BigDecimal("550000");
        BigDecimal advance = new BigDecimal("3000000");
        BigDecimal other = new BigDecimal("500000");

        BigDecimal net = engine.calculateNetSalary(gross, employeeInsurance, pit, advance, other);

        // 25M - 2.1M - 0.55M - 3M - 0.5M = 18,850,000
        assertThat(net).isEqualByComparingTo(new BigDecimal("18850000.00"));
    }
}

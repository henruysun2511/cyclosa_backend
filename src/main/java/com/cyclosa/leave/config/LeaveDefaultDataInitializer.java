package com.cyclosa.leave.config;

import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.entity.SickLeaveEntitlementTier;
import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.LeaveCategory;
import com.cyclosa.leave.repository.LeaveTypeRepository;
import com.cyclosa.leave.repository.SickLeaveEntitlementTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class LeaveDefaultDataInitializer implements CommandLineRunner {

    private final LeaveTypeRepository leaveTypeRepository;
    private final SickLeaveEntitlementTierRepository sickTierRepository;

    @Override
    public void run(String... args) {
        seedDefaultLeaveTypes();
        seedSickLeaveTiers();
    }

    private void seedDefaultLeaveTypes() {
        List<LeaveType> defaults = List.of(
                LeaveType.builder()
                        .name("Nghỉ phép năm")
                        .code("ANNUAL_LEAVE")
                        .category(LeaveCategory.ANNUAL)
                        .fundingSource(FundingSource.COMPANY)
                        .isPaid(true)
                        .requiresApproval(true)
                        .isActive(true)
                        .description("Nghỉ hàng năm hưởng nguyên lương theo Điều 113 BLLĐ 2019")
                        .build(),
                LeaveType.builder()
                        .name("Nghỉ Lễ, Tết")
                        .code("PUBLIC_HOLIDAY")
                        .category(LeaveCategory.PUBLIC_HOLIDAY)
                        .fundingSource(FundingSource.COMPANY)
                        .isPaid(true)
                        .requiresApproval(false)
                        .isActive(true)
                        .description("Nghỉ các ngày lễ tết quốc gia theo Điều 112 BLLĐ 2019")
                        .build(),
                LeaveType.builder()
                        .name("Bản thân kết hôn")
                        .code("MARRIAGE_LEAVE")
                        .category(LeaveCategory.PERSONAL_PAID)
                        .fundingSource(FundingSource.COMPANY)
                        .fixedDaysPerEvent(BigDecimal.valueOf(3.0))
                        .isPaid(true)
                        .requiresApproval(true)
                        .isActive(true)
                        .description("Bản thân kết hôn nghỉ 03 ngày nguyên lương theo Điều 115.1 BLLĐ")
                        .build(),
                LeaveType.builder()
                        .name("Con kết hôn")
                        .code("CHILD_MARRIAGE_LEAVE")
                        .category(LeaveCategory.PERSONAL_PAID)
                        .fundingSource(FundingSource.COMPANY)
                        .fixedDaysPerEvent(BigDecimal.valueOf(1.0))
                        .isPaid(true)
                        .requiresApproval(true)
                        .isActive(true)
                        .description("Con đẻ, con nuôi kết hôn nghỉ 01 ngày nguyên lương theo Điều 115.1 BLLĐ")
                        .build(),
                LeaveType.builder()
                        .name("Tang chế thân tộc trực hệ")
                        .code("FUNERAL_DIRECT_LEAVE")
                        .category(LeaveCategory.PERSONAL_PAID)
                        .fundingSource(FundingSource.COMPANY)
                        .fixedDaysPerEvent(BigDecimal.valueOf(3.0))
                        .isPaid(true)
                        .requiresApproval(true)
                        .isActive(true)
                        .description("Cha/mẹ/vợ/chồng/con chết nghỉ 03 ngày nguyên lương theo Điều 115.1 BLLĐ")
                        .build(),
                LeaveType.builder()
                        .name("Nghỉ việc riêng không lương")
                        .code("UNPAID_LEAVE")
                        .category(LeaveCategory.PERSONAL_UNPAID)
                        .fundingSource(FundingSource.COMPANY)
                        .isPaid(false)
                        .requiresApproval(true)
                        .isActive(true)
                        .description("Nghỉ việc riêng không hưởng lương theo thỏa thuận Điều 115.3 BLLĐ")
                        .build(),
                LeaveType.builder()
                        .name("Nghỉ ốm đau (Hưởng BHXH)")
                        .code("SICK_LEAVE_INSURANCE")
                        .category(LeaveCategory.SICK)
                        .fundingSource(FundingSource.SOCIAL_INSURANCE_FUND)
                        .isPaid(false)
                        .requiresApproval(true)
                        .isActive(true)
                        .description("Chế độ ốm đau do Quỹ BHXH chi trả theo Luật BHXH")
                        .build(),
                LeaveType.builder()
                        .name("Nghỉ thai sản (Hưởng BHXH)")
                        .code("MATERNITY_LEAVE")
                        .category(LeaveCategory.MATERNITY)
                        .fundingSource(FundingSource.SOCIAL_INSURANCE_FUND)
                        .isPaid(false)
                        .requiresApproval(true)
                        .isActive(true)
                        .description("Chế độ thai sản do Quỹ BHXH chi trả theo Luật BHXH")
                        .build()
        );

        for (LeaveType lt : defaults) {
            if (leaveTypeRepository.findByCodeAndCompanyIdIsNull(lt.getCode()).isEmpty()) {
                leaveTypeRepository.save(lt);
                log.info("Seeded default system LeaveType code={}", lt.getCode());
            }
        }
    }

    private void seedSickLeaveTiers() {
        if (sickTierRepository.count() == 0) {
            List<SickLeaveEntitlementTier> tiers = List.of(
                    SickLeaveEntitlementTier.builder()
                            .minBhxhYears(BigDecimal.ZERO)
                            .maxBhxhYears(BigDecimal.valueOf(15.0))
                            .normalConditionMaxDays(30)
                            .hazardousConditionMaxDays(40)
                            .build(),
                    SickLeaveEntitlementTier.builder()
                            .minBhxhYears(BigDecimal.valueOf(15.0))
                            .maxBhxhYears(BigDecimal.valueOf(30.0))
                            .normalConditionMaxDays(40)
                            .hazardousConditionMaxDays(50)
                            .build(),
                    SickLeaveEntitlementTier.builder()
                            .minBhxhYears(BigDecimal.valueOf(30.0))
                            .maxBhxhYears(null)
                            .normalConditionMaxDays(60)
                            .hazardousConditionMaxDays(70)
                            .build()
            );
            sickTierRepository.saveAll(tiers);
            log.info("Seeded 3 default sick leave entitlement tiers per Vietnam Social Insurance Law");
        }
    }
}

package com.cyclosa.contract.validator;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.enums.TerminationGround;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Validator chuyên trách các điều luật bắt buộc của Bộ luật Lao động 2019 và Nghị định 145/2020/NĐ-CP.
 * Áp dụng triết lý Hard Validation (chặn cứng vi phạm pháp luật).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractLegalValidator {

    private final ContractRepository contractRepository;

    /**
     * Kiểm tra Điều 20: Thời hạn hợp đồng lao động.
     * - HĐ không xác định thời hạn: không được có endDate.
     * - HĐ xác định thời hạn: bắt buộc có endDate và không được vượt quá 36 tháng.
     */
    public void validateContractTypeAndDuration(ContractType contractType, LocalDate startDate, LocalDate endDate) {
        if (contractType == ContractType.INDEFINITE_TERM) {
            if (endDate != null) {
                log.warn("Vi phạm Điều 20 BLLĐ: HĐ không xác định thời hạn nhưng lại có end_date={}", endDate);
                throw new AppException(ContractErrorCode.INDEFINITE_CONTRACT_CANNOT_HAVE_END_DATE);
            }
            return;
        }

        if (endDate == null) {
            log.warn("Vi phạm Điều 20 BLLĐ: HĐ xác định thời hạn/thử việc nhưng không có end_date");
            throw new AppException(ContractErrorCode.DEFINITE_CONTRACT_REQUIRES_END_DATE);
        }

        if (!endDate.isAfter(startDate)) {
            throw new AppException(ContractErrorCode.INVALID_CONTRACT_STATUS_TRANSITION, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        if (contractType == ContractType.DEFINITE_TERM) {
            Period period = Period.between(startDate, endDate);
            long totalMonths = period.toTotalMonths();
            // Nếu đủ 36 tháng nhưng còn lẻ thêm ngày thì vượt quá 36 tháng
            if (totalMonths > 36 || (totalMonths == 36 && period.getDays() > 0)) {
                log.error("Vi phạm Điều 20 BLLĐ 2019: Thời hạn HĐ xác định thời hạn vượt quá 36 tháng ({} tháng, {} ngày)", totalMonths, period.getDays());
                throw new AppException(ContractErrorCode.CONTRACT_DURATION_EXCEEDS_LIMIT);
            }
        }
    }

    /**
     * Kiểm tra Điều 20.2: Giới hạn số lần ký HĐ xác định thời hạn (Tối đa 1 lần gia hạn, tức tối đa 2 HĐ).
     */
    public void validateDefiniteContractRenewalLimit(UUID employeeId, UUID companyId, ContractType newContractType) {
        if (newContractType != ContractType.DEFINITE_TERM) {
            return;
        }

        long existingDefiniteContracts = contractRepository.countDefiniteContracts(employeeId, companyId, ContractType.DEFINITE_TERM);
        if (existingDefiniteContracts >= 2) {
            log.error("Vi phạm Điều 20.2 BLLĐ 2019: Nhân viên id={} đã ký {} lần HĐ xác định thời hạn. Bắt buộc phải ký HĐ không xác định thời hạn!",
                    employeeId, existingDefiniteContracts);
            throw new AppException(ContractErrorCode.MAX_DEFINITE_CONTRACTS_EXCEEDED);
        }
    }

    /**
     * Kiểm tra Điều 24, 25, 26: Thử việc.
     * @param maxAllowedDays số ngày tối đa cho phép theo cấp bậc (180, 60, 30, 6)
     * @param probationSalary mức lương thử việc
     * @param officialSalary mức lương chính thức
     */
    public void validateProbationRules(UUID employeeId, LocalDate startDate, LocalDate endDate,
                                       int maxAllowedDays, BigDecimal probationSalary, BigDecimal officialSalary) {
        long probationDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (probationDays > maxAllowedDays) {
            log.error("Vi phạm Điều 25 BLLĐ 2019: Số ngày thử việc {} ngày vượt quá hạn mức {} ngày cho cấp bậc này", probationDays, maxAllowedDays);
            throw new AppException(ContractErrorCode.PROBATION_DURATION_EXCEEDS_LIMIT);
        }

        // Lương thử việc ít nhất 85% lương chính thức (Điều 26)
        if (officialSalary != null && officialSalary.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal minLegalSalary = officialSalary.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP);
            if (probationSalary.compareTo(minLegalSalary) < 0) {
                log.error("Vi phạm Điều 26 BLLĐ 2019: Lương thử việc {} thấp hơn 85% lương chính thức {} (tối thiểu {})",
                        probationSalary, officialSalary, minLegalSalary);
                throw new AppException(ContractErrorCode.PROBATION_SALARY_BELOW_LEGAL_MINIMUM);
            }
        }

        // Kiểm tra thử việc 1 lần (Điều 24.2)
        boolean alreadyProbated = contractRepository.existsByEmployeeIdAndContractType(employeeId, ContractType.PROBATION);
        if (alreadyProbated) {
            log.error("Vi phạm Điều 24.2 BLLĐ 2019: Nhân viên id={} đã từng có Hợp đồng thử việc tại công ty", employeeId);
            throw new AppException(ContractErrorCode.PROBATION_ALREADY_COMPLETED);
        }
    }

    /**
     * Kiểm tra tính duy nhất: 1 nhân viên chỉ có tối đa 1 hợp đồng ACTIVE.
     */
    public void validateNoExistingActiveContract(UUID employeeId, UUID excludeContractId) {
        contractRepository.findByEmployeeIdAndContractStatus(employeeId, ContractStatus.ACTIVE)
                .ifPresent(existingContract -> {
                    if (excludeContractId == null || !existingContract.getId().equals(excludeContractId)) {
                        log.error("Nhân viên id={} đã có hợp đồng active số '{}'", employeeId, existingContract.getContractNumber());
                        throw new AppException(ContractErrorCode.ACTIVE_CONTRACT_ALREADY_EXISTS);
                    }
                });
    }

    /**
     * Kiểm tra Điều 37 & Điều 137.3: Cấm NSDLĐ đơn phương chấm dứt đối với trường hợp được bảo vệ đặc biệt.
     */
    public void validateTerminationRestrictions(TerminationGround ground, boolean isPregnantOrMaternity, boolean hasChildUnder12Months, boolean isOnSickLeave) {
        // Chỉ áp dụng khi NSDLĐ đơn phương chấm dứt
        if (ground == TerminationGround.EMPLOYER_REGULAR || ground == TerminationGround.EMPLOYER_ILLEGAL) {
            if (isPregnantOrMaternity) {
                log.error("Vi phạm Điều 37.3 & 137.3 BLLĐ 2019: Cấm chấm dứt hợp đồng với lao động nữ mang thai hoặc nghỉ thai sản!");
                throw new AppException(ContractErrorCode.TERMINATION_RESTRICTED_BY_LAW, "Lao động nữ đang mang thai hoặc nghỉ thai sản");
            }
            if (hasChildUnder12Months) {
                log.error("Vi phạm Điều 37.3 & 137.3 BLLĐ 2019: Cấm chấm dứt hợp đồng với nhân sự đang nuôi con dưới 12 tháng tuổi!");
                throw new AppException(ContractErrorCode.TERMINATION_RESTRICTED_BY_LAW, "Nhân sự đang nuôi con nhỏ dưới 12 tháng tuổi");
            }
            if (isOnSickLeave) {
                log.error("Vi phạm Điều 37.1 BLLĐ 2019: Cấm chấm dứt hợp đồng khi người lao động đang nghỉ điều trị ốm đau/bệnh tật có chỉ định y tế!");
                throw new AppException(ContractErrorCode.TERMINATION_RESTRICTED_BY_LAW, "Người lao động đang nghỉ điều trị ốm đau, bệnh tật");
            }
        }
    }

    /**
     * Kiểm tra thời hạn báo trước theo Điều 35, 36.
     * @return true nếu báo trước hợp lệ, false nếu vi phạm thời hạn báo trước
     */
    public boolean checkNoticePeriod(Contract contract, TerminationGround ground, LocalDate noticeDate, LocalDate finalWorkingDate) {
        if (ground == TerminationGround.EXPIRED || ground == TerminationGround.TASK_COMPLETED
                || ground == TerminationGround.MUTUAL_AGREEMENT || ground == TerminationGround.EMPLOYEE_SPECIAL
                || ground == TerminationGround.DISCIPLINARY || ground == TerminationGround.FORCE_MAJEURE_DEATH
                || ground == TerminationGround.COMPANY_DISSOLUTION) {
            return true;
        }

        long daysNotice = ChronoUnit.DAYS.between(noticeDate, finalWorkingDate);
        int requiredDays = getRequiredNoticeDays(contract);

        return daysNotice >= requiredDays;
    }

    public int getRequiredNoticeDays(Contract contract) {
        if (contract.getContractType() == ContractType.INDEFINITE_TERM) {
            return 45; // Ít nhất 45 ngày (Điều 35.1.a, 36.2.a)
        }
        if (contract.getContractType() == ContractType.DEFINITE_TERM) {
            if (contract.getEndDate() != null) {
                long months = ChronoUnit.MONTHS.between(contract.getStartDate(), contract.getEndDate());
                if (months >= 12) {
                    return 30; // Ít nhất 30 ngày (Điều 35.1.b, 36.2.b)
                }
            }
            return 3; // Ít nhất 3 ngày làm việc nếu < 12 tháng
        }
        return 3;
    }
}

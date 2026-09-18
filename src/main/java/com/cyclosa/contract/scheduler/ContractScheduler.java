package com.cyclosa.contract.scheduler;

import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler giám sát hạn hợp đồng lao động và tự động chuyển đổi theo Điều 20.2.b BLLĐ 2019.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractScheduler {

    private final ContractRepository contractRepository;

    /**
     * Chạy định kỳ lúc 02:00 AM hàng ngày: Quét hợp đồng sắp hết hạn trong 30 ngày để gắn cờ EXPIRING_SOON.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scanExpiringContracts() {
        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);

        List<Contract> expiring = contractRepository.findExpiringContracts(today, next30Days);
        for (Contract contract : expiring) {
            if (contract.getContractStatus() == ContractStatus.ACTIVE) {
                contract.setContractStatus(ContractStatus.EXPIRING_SOON);
                contractRepository.save(contract);
                log.info("[Scheduler] Đã đánh dấu EXPIRING_SOON cho Hợp đồng số '{}' của nhân viên id={}",
                        contract.getContractNumber(), contract.getEmployeeId());
            }
        }
    }

    /**
     * Chạy định kỳ lúc 03:00 AM hàng ngày:
     * Điều 20.2.b BLLĐ 2019: Sau 30 ngày kể từ ngày HĐLĐ hết hạn mà 2 bên chưa ký mới,
     * HĐ tự động trở thành HĐ không xác định thời hạn.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void autoConvertIndefiniteContracts() {
        LocalDate thresholdDate = LocalDate.now().minusDays(30);
        List<Contract> pending = contractRepository.findContractsPendingIndefiniteConversion(thresholdDate);

        for (Contract contract : pending) {
            contract.setContractType(ContractType.INDEFINITE_TERM);
            contract.setEndDate(null);
            contract.setContractStatus(ContractStatus.ACTIVE);
            contract.setNote((contract.getNote() != null ? contract.getNote() + "\n" : "") +
                    "[Tự động chuyển đổi]: Chuyển sang HĐ Không xác định thời hạn theo Điều 20.2.b BLLĐ 2019 do quá 30 ngày chưa ký HĐ mới.");
            contractRepository.save(contract);

            log.warn("[Scheduler] Đã TỰ ĐỘNG CHUYỂN ĐỔI Hợp đồng số '{}' sang KHÔNG XÁC ĐỊNH THỜI HẠN theo Điều 20.2.b BLLĐ 2019!",
                    contract.getContractNumber());
        }
    }
}

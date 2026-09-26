package com.cyclosa.discipline.scheduler;

import com.cyclosa.discipline.service.RewardDisciplineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler quét và tự động xóa kỷ luật lao động khi hết thời hạn theo Điều 126 BLLĐ 2019.
 * (3 tháng đối với Khiển trách, 6 tháng đối với Kéo dài thời hạn nâng lương hoặc Cách chức).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisciplineScheduler {

    private final RewardDisciplineService rewardDisciplineService;

    /**
     * Chạy định kỳ lúc 02:30 AM hàng ngày: Quét các quyết định kỷ luật đã hết thời hạn xóa kỷ luật và chuyển sang trạng thái EXPIRED.
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void autoExpireDisciplines() {
        log.info("[Scheduler] Bắt đầu quét và tự động xóa kỷ luật theo Điều 126 BLLĐ 2019...");
        int count = rewardDisciplineService.autoExpireDisciplines();
        log.info("[Scheduler] Đã tự động cập nhật trạng thái EXPIRED cho {} hồ sơ kỷ luật hết hiệu lực.", count);
    }
}

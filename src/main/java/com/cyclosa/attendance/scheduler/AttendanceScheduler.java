package com.cyclosa.attendance.scheduler;

import com.cyclosa.attendance.service.AttendanceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduler tự động hóa chốt công cuối ngày và rà soát các trường hợp quên dập thẻ / vắng mặt
 * theo Điều 105 - 116 Bộ luật Lao động 2019.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRecordService attendanceRecordService;

    /**
     * Chạy định kỳ lúc 01:00 AM hàng ngày:
     * Quét toàn bộ dữ liệu chấm công của ngày hôm trước (yesterday):
     * 1. Chuyển các lượt Check-in quên Check-out sang trạng thái MISSING_CHECK_OUT (0 công).
     * 2. Tự động tạo bản ghi ABSENT cho nhân viên có lịch phân ca (ShiftAssignment) nhưng không điểm danh.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void sweepYesterdayAttendance() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[AttendanceScheduler] Bắt đầu tiến trình tự động chốt công cho ngày: {}", yesterday);
        try {
            attendanceRecordService.sweepDailyAttendanceRecords(yesterday);
            log.info("[AttendanceScheduler] Hoàn tất tiến trình chốt công tự động cho ngày: {}", yesterday);
        } catch (Exception ex) {
            log.error("[AttendanceScheduler] Lỗi khi thực hiện chốt công tự động cho ngày {}: {}", yesterday, ex.getMessage(), ex);
        }
    }
}

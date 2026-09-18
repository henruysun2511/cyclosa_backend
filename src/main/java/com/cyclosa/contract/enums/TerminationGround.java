package com.cyclosa.contract.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 13 Căn cứ chấm dứt Hợp đồng lao động hợp pháp theo Điều 34, 35, 36 Bộ luật Lao động 2019.
 */
@Getter
@RequiredArgsConstructor
public enum TerminationGround {

    EXPIRED("Hết hạn hợp đồng lao động (Điều 34.1)", 0, true, false),
    TASK_COMPLETED("Đã hoàn thành công việc theo hợp đồng (Điều 34.2)", 0, true, false),
    MUTUAL_AGREEMENT("Hai bên thỏa thuận chấm dứt hợp đồng (Điều 34.3)", 0, true, false),
    EMPLOYEE_REGULAR("Người lao động đơn phương chấm dứt hợp pháp (Điều 35.1)", 30, true, false),
    EMPLOYEE_SPECIAL("Người lao động đơn phương không cần báo trước (Điều 35.2)", 0, true, false),
    EMPLOYEE_ILLEGAL("Người lao động đơn phương chấm dứt trái pháp luật (Điều 39)", 0, false, false),
    EMPLOYER_REGULAR("Người sử dụng lao động đơn phương chấm dứt đúng luật (Điều 36.1)", 30, true, false),
    EMPLOYER_ILLEGAL("Người sử dụng lao động đơn phương chấm dứt trái luật (Điều 41)", 0, true, false),
    DISCIPLINARY("Bị xử lý kỷ luật sa thải (Điều 125)", 0, false, false),
    RESTRUCTURING("Cho thôi việc do thay đổi cơ cấu, công nghệ (Điều 42)", 15, false, true),
    MERGER_ACQUISITION("Cho thôi việc do chia, tách, hợp nhất, sáp nhập (Điều 43)", 15, false, true),
    FORCE_MAJEURE_DEATH("Người lao động chết, mất tích hoặc mất năng lực hành vi (Điều 34.7)", 0, true, false),
    COMPANY_DISSOLUTION("Người sử dụng lao động chấm dứt hoạt động, phá sản (Điều 34.8)", 0, true, false);

    private final String description;
    private final int defaultNoticeDays; // Thời hạn báo trước mặc định (ngày)
    private final boolean eligibleForSeverance; // Hưởng trợ cấp thôi việc (Điều 46)
    private final boolean eligibleForJobLoss;   // Hưởng trợ cấp mất việc làm (Điều 47)
}

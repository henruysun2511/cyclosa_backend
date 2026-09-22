package com.cyclosa.leave.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Trạng thái của đơn xin nghỉ phép.
 */
@Getter
@RequiredArgsConstructor
public enum LeaveRequestStatus {
    DRAFT("Bản nháp"),
    PENDING_APPROVAL("Chờ phê duyệt"),
    APPROVED("Đã phê duyệt"),
    REJECTED("Bị từ chối"),
    CANCELLED("Đã hủy");

    private final String description;
}

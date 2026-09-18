package com.cyclosa.contract.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Vòng đời trạng thái của Hợp đồng lao động.
 */
@Getter
@RequiredArgsConstructor
public enum ContractStatus {
    DRAFT("Dự thảo"),
    PENDING_APPROVAL("Đang chờ duyệt"),
    APPROVED("Đã phê duyệt"),
    ACTIVE("Đang có hiệu lực"),
    EXPIRING_SOON("Sắp hết hạn"),
    RENEWED("Đã gia hạn / Tái ký"),
    EXPIRED("Đã hết hạn"),
    TERMINATED("Đã chấm dứt / Thanh lý");

    private final String description;
}

package com.cyclosa.contract.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Phân loại loại Hợp đồng lao động hợp pháp theo Điều 20 Bộ luật Lao động 2019.
 */
@Getter
@RequiredArgsConstructor
public enum ContractType {
    INDEFINITE_TERM("Hợp đồng lao động không xác định thời hạn"),
    DEFINITE_TERM("Hợp đồng lao động xác định thời hạn (tối đa 36 tháng)"),
    PROBATION("Hợp đồng thử việc");

    private final String description;
}

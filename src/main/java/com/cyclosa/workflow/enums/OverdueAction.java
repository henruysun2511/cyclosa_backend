package com.cyclosa.workflow.enums;

/**
 * Hành vi xử lý khi bước phê duyệt bị quá hạn SLA.
 */
public enum OverdueAction {
    REMIND,
    AUTO_ESCALATE,
    AUTO_REJECT
}

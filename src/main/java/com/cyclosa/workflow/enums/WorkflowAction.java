package com.cyclosa.workflow.enums;

/**
 * Hành động tại từng bước phê duyệt (WorkflowApprovalStep).
 */
public enum WorkflowAction {
    PENDING,
    APPROVED,
    REJECTED,
    DELEGATED,
    AUTO_ESCALATED,
    AUTO_REJECTED
}

package com.cyclosa.payroll.listener;

import com.cyclosa.payroll.service.PayrollPeriodService;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.ApprovalStatus;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollApprovalListener {

    private final PayrollPeriodService periodService;

    @Async
    @EventListener
    public void onWorkflowCompleted(WorkflowCompletedEvent event) {
        if (event.getRequestType() != ApprovalRequestType.PAYROLL_APPROVAL) {
            return;
        }

        log.info("Handling PayrollPeriod WorkflowCompletedEvent: periodId={}, finalStatus={}",
                event.getRequestId(), event.getFinalStatus());

        try {
            if (event.getFinalStatus() == ApprovalStatus.APPROVED) {
                periodService.approvePeriod(event.getCompanyId(), event.getRequestId());
            }
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý PayrollPeriod WorkflowCompletedEvent cho id={}: {}",
                    event.getRequestId(), ex.getMessage(), ex);
        }
    }
}

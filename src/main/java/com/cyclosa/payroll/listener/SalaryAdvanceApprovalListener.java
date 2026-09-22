package com.cyclosa.payroll.listener;

import com.cyclosa.payroll.service.SalaryAdvanceService;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SalaryAdvanceApprovalListener {

    private final SalaryAdvanceService advanceService;

    @Async
    @EventListener
    public void onWorkflowCompleted(WorkflowCompletedEvent event) {
        if (event.getRequestType() != ApprovalRequestType.SALARY_ADVANCE) {
            return;
        }

        log.info("Handling SalaryAdvance WorkflowCompletedEvent: advanceId={}, finalStatus={}",
                event.getRequestId(), event.getFinalStatus());

        try {
            advanceService.handleWorkflowCompleted(event.getRequestId(), event.getFinalStatus());
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý SalaryAdvance WorkflowCompletedEvent cho id={}: {}",
                    event.getRequestId(), ex.getMessage(), ex);
        }
    }
}

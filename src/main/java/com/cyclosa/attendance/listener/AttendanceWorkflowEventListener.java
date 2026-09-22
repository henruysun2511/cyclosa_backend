package com.cyclosa.attendance.listener;

import com.cyclosa.attendance.service.AttendanceExplanationService;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceWorkflowEventListener {

    private final AttendanceExplanationService explanationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWorkflowCompleted(WorkflowCompletedEvent event) {
        if (event.getRequestType() != ApprovalRequestType.ATTENDANCE_CORRECTION) {
            return;
        }

        log.info("Received WorkflowCompletedEvent for ATTENDANCE_CORRECTION: explanationId={}, status={}",
                event.getRequestId(), event.getFinalStatus());

        explanationService.handleWorkflowCompleted(event.getRequestId(), event.getFinalStatus());
    }
}

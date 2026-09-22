package com.cyclosa.leave.listener;

import com.cyclosa.leave.service.LeaveRequestService;
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
public class LeaveWorkflowListener {

    private final LeaveRequestService leaveRequestService;

    @Async
    @EventListener
    public void onWorkflowCompleted(WorkflowCompletedEvent event) {
        if (event.getRequestType() != ApprovalRequestType.LEAVE_REQUEST) {
            return;
        }

        log.info("Handling LeaveRequest WorkflowCompletedEvent: requestId={}, finalStatus={}",
                event.getRequestId(), event.getFinalStatus());

        try {
            leaveRequestService.handleWorkflowCompleted(event.getRequestId(), event.getFinalStatus(), event.getReason());
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý LeaveRequest WorkflowCompletedEvent cho id={}: {}",
                    event.getRequestId(), ex.getMessage(), ex);
        }
    }
}

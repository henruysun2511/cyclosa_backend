package com.cyclosa.recruitment.listener;

import com.cyclosa.recruitment.enums.ManpowerRequestStatus;
import com.cyclosa.recruitment.repository.ManpowerRequestRepository;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.ApprovalStatus;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecruitmentWorkflowEventListener {

    private final ManpowerRequestRepository manpowerRequestRepository;

    @Async
    @EventListener
    @Transactional
    public void handleWorkflowCompletedEvent(WorkflowCompletedEvent event) {
        if (event.getRequestType() != ApprovalRequestType.MANPOWER_REQUEST) {
            return;
        }

        log.info("Nhận WorkflowCompletedEvent cho MANPOWER_REQUEST: requestId={}, finalStatus={}",
                event.getRequestId(), event.getFinalStatus());

        try {
            UUID requestId = event.getRequestId();
            if (requestId == null) {
                return;
            }
            manpowerRequestRepository.findById(requestId).ifPresent(req -> {
                if (event.getFinalStatus() == ApprovalStatus.APPROVED) {
                    req.setStatus(ManpowerRequestStatus.APPROVED);
                } else if (event.getFinalStatus() == ApprovalStatus.REJECTED) {
                    req.setStatus(ManpowerRequestStatus.REJECTED);
                }
                manpowerRequestRepository.save(req);
                log.info("Cập nhật ManpowerRequest {} sang trạng thái {}", requestId, req.getStatus());
            });
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái ManpowerRequest từ WorkflowCompletedEvent: {}", e.getMessage(), e);
        }
    }
}

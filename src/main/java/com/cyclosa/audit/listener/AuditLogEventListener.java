package com.cyclosa.audit.listener;

import com.cyclosa.audit.dto.request.AuditLogCommand;
import com.cyclosa.audit.event.AuditLogEvent;
import com.cyclosa.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private final AuditLogService auditLogService;

    @Async
    @EventListener
    public void onAuditLogEvent(AuditLogEvent event) {
        AuditLogCommand command = AuditLogCommand.builder()
                .userId(event.getUserId())
                .action(event.getAction())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .oldValue(event.getOldValue())
                .newValue(event.getNewValue())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .build();
        auditLogService.log(command);
    }
}

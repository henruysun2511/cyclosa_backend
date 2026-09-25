package com.cyclosa.audit.service;

import com.cyclosa.audit.dto.request.AuditLogCommand;
import com.cyclosa.audit.dto.request.AuditLogFilter;
import com.cyclosa.audit.dto.response.AuditLogDetailResponse;
import com.cyclosa.audit.dto.response.AuditLogResponse;
import com.cyclosa.common.response.PageData;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditLogService {

    void log(AuditLogCommand command);

    void log(UUID userId, String action, String entityType, UUID entityId, String oldValue, String newValue);

    void log(UUID userId, String action, String entityType, UUID entityId, String oldValue, String newValue, String ipAddress, String userAgent);

    PageData<AuditLogResponse> getLogs(AuditLogFilter filter);

    AuditLogDetailResponse getLogById(UUID id);

    PageData<AuditLogResponse> getLogsByEntity(String entityType, UUID entityId, Pageable pageable);
}

package com.cyclosa.audit.service.impl;

import com.cyclosa.audit.dto.request.AuditLogCommand;
import com.cyclosa.audit.dto.request.AuditLogFilter;
import com.cyclosa.audit.dto.response.AuditLogDetailResponse;
import com.cyclosa.audit.dto.response.AuditLogResponse;
import com.cyclosa.audit.entity.AuditLog;
import com.cyclosa.audit.mapper.AuditLogMapper;
import com.cyclosa.audit.repository.AuditLogRepository;
import com.cyclosa.audit.service.AuditLogService;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.dto.summary.UserSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final AuditLogMapper auditLogMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "action", "entityType");

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditLogCommand command) {
        try {
            AuditLog entity = auditLogMapper.toEntity(command);
            auditLogRepository.save(entity);
            log.debug("[AUDIT] Ghi nhận log: action={}, entityType={}, entityId={}, userId={}",
                    command.getAction(), command.getEntityType(), command.getEntityId(), command.getUserId());
        } catch (Exception e) {
            log.error("[AUDIT] Lỗi khi ghi nhật ký kiểm toán: {}", e.getMessage(), e);
        }
    }

    @Override
    public void log(UUID userId, String action, String entityType, UUID entityId, String oldValue, String newValue) {
        log(userId, action, entityType, entityId, oldValue, newValue, null, null);
    }

    @Override
    public void log(UUID userId, String action, String entityType, UUID entityId,
                    String oldValue, String newValue, String ipAddress, String userAgent) {
        AuditLogCommand command = AuditLogCommand.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        log(command);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<AuditLogResponse> getLogs(AuditLogFilter filter) {
        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);

        LocalDateTime fromDateTime = filter.getFromDate() != null ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = filter.getToDate() != null ? filter.getToDate().atTime(LocalTime.MAX) : null;

        Page<AuditLog> page = auditLogRepository.search(
                filter.getUserId(),
                filter.getAction(),
                filter.getEntityType(),
                filter.getEntityId(),
                fromDateTime,
                toDateTime,
                kw,
                pageable
        );

        return enrichWithUsers(page);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDetailResponse getLogById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy nhật ký kiểm toán với ID: " + id));

        AuditLogDetailResponse response = auditLogMapper.toDetailResponse(auditLog);
        if (auditLog.getUserId() != null) {
            Map<UUID, UserSummary> userMap = userService.getUserSummaries(Set.of(auditLog.getUserId()));
            response.setUser(userMap.get(auditLog.getUserId()));
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<AuditLogResponse> getLogsByEntity(String entityType, UUID entityId, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType,
                entityId,
                pageable
        );
        return enrichWithUsers(page);
    }

    private PageData<AuditLogResponse> enrichWithUsers(Page<AuditLog> page) {
        Set<UUID> userIds = page.getContent().stream()
                .map(AuditLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, UserSummary> userMap = userService.getUserSummaries(userIds);

        List<AuditLogResponse> responses = page.getContent().stream().map(logEntity -> {
            AuditLogResponse res = auditLogMapper.toResponse(logEntity);
            if (logEntity.getUserId() != null) {
                res.setUser(userMap.get(logEntity.getUserId()));
            }
            return res;
        }).toList();

        return PageData.of(page, responses);
    }
}

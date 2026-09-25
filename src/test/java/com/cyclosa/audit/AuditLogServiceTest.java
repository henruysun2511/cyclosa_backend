package com.cyclosa.audit;

import com.cyclosa.audit.dto.request.AuditLogCommand;
import com.cyclosa.audit.dto.request.AuditLogFilter;
import com.cyclosa.audit.dto.response.AuditLogDetailResponse;
import com.cyclosa.audit.dto.response.AuditLogResponse;
import com.cyclosa.audit.entity.AuditLog;
import com.cyclosa.audit.mapper.AuditLogMapper;
import com.cyclosa.audit.repository.AuditLogRepository;
import com.cyclosa.audit.service.impl.AuditLogServiceImpl;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.dto.summary.UserSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private UUID userId;
    private UUID logId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        logId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Ghi log kiểm toán thành công")
    void log_Success() {
        AuditLogCommand cmd = AuditLogCommand.builder()
                .userId(userId)
                .action("APPROVE")
                .entityType("LEAVE_REQUEST")
                .entityId(UUID.randomUUID())
                .oldValue("{\"status\":\"PENDING\"}")
                .newValue("{\"status\":\"APPROVED\"}")
                .ipAddress("127.0.0.1")
                .build();

        AuditLog entity = AuditLog.builder()
                .userId(userId)
                .action("APPROVE")
                .entityType("LEAVE_REQUEST")
                .build();

        when(auditLogMapper.toEntity(cmd)).thenReturn(entity);
        when(auditLogRepository.save(entity)).thenReturn(entity);

        auditLogService.log(cmd);

        verify(auditLogRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Tra cứu danh sách log kiểm toán và làm giàu thông tin User (Batch Enrichment)")
    void getLogs_Success_WithBatchEnrichment() {
        AuditLogFilter filter = new AuditLogFilter();
        filter.setAction("UPDATE");
        filter.setKeyword("ROLE");

        AuditLog entity = AuditLog.builder()
                .id(logId)
                .userId(userId)
                .action("UPDATE")
                .entityType("ROLE")
                .createdAt(LocalDateTime.now())
                .build();

        AuditLogResponse responseDto = AuditLogResponse.builder()
                .id(logId)
                .action("UPDATE")
                .entityType("ROLE")
                .build();

        UserSummary userSummary = UserSummary.builder()
                .id(userId)
                .username("admin")
                .fullName("Quản trị viên")
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(entity));
        when(auditLogRepository.search(any(), eq("UPDATE"), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(userService.getUserSummaries(Set.of(userId)))
                .thenReturn(Map.of(userId, userSummary));
        when(auditLogMapper.toResponse(entity)).thenReturn(responseDto);

        PageData<AuditLogResponse> result = auditLogService.getLogs(filter);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getUser()).isNotNull();
        assertThat(result.getItems().get(0).getUser().getUsername()).isEqualTo("admin");
        verify(userService, times(1)).getUserSummaries(Set.of(userId));
    }

    @Test
    @DisplayName("Xem chi tiết log kiểm toán theo ID")
    void getLogById_Success() {
        AuditLog entity = AuditLog.builder()
                .id(logId)
                .userId(userId)
                .action("DELETE")
                .entityType("CONTRACT")
                .build();

        AuditLogDetailResponse detailDto = AuditLogDetailResponse.builder()
                .id(logId)
                .action("DELETE")
                .entityType("CONTRACT")
                .build();

        UserSummary userSummary = UserSummary.builder()
                .id(userId)
                .username("admin")
                .build();

        when(auditLogRepository.findById(logId)).thenReturn(Optional.of(entity));
        when(auditLogMapper.toDetailResponse(entity)).thenReturn(detailDto);
        when(userService.getUserSummaries(Set.of(userId))).thenReturn(Map.of(userId, userSummary));

        AuditLogDetailResponse res = auditLogService.getLogById(logId);

        assertThat(res).isNotNull();
        assertThat(res.getAction()).isEqualTo("DELETE");
        assertThat(res.getUser()).isNotNull();
    }

    @Test
    @DisplayName("Xem chi tiết log thất bại khi ID không tồn tại")
    void getLogById_NotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(auditLogRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.getLogById(nonExistentId))
                .isInstanceOf(AppException.class);
    }
}

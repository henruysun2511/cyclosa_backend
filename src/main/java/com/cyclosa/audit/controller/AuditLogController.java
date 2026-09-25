package com.cyclosa.audit.controller;

import com.cyclosa.audit.dto.request.AuditLogFilter;
import com.cyclosa.audit.dto.response.AuditLogDetailResponse;
import com.cyclosa.audit.dto.response.AuditLogResponse;
import com.cyclosa.audit.service.AuditLogService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "AuditLogs", description = "Quản lý và tra cứu nhật ký kiểm toán hệ thống (Audit Trail)")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("@perm.has('audit.view')")
    @RequirePermission("audit.view")
    @Operation(summary = "Tra cứu nhật ký kiểm toán", description = "Tìm kiếm và phân trang nhật ký thao tác hệ thống theo người dùng, hành động, loại thực thể và khoảng thời gian")
    public ResponseEntity<ApiResponse<PageData<AuditLogResponse>>> getLogs(
            @Valid AuditLogFilter filter
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditLogService.getLogs(filter),
                "Tra cứu danh sách nhật ký kiểm toán thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('audit.view')")
    @RequirePermission("audit.view")
    @Operation(summary = "Xem chi tiết nhật ký kiểm toán", description = "Xem chi tiết một bản ghi nhật ký kiểm toán gồm dữ liệu trước/sau (diff) và thông tin thiết bị")
    public ResponseEntity<ApiResponse<AuditLogDetailResponse>> getLogById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditLogService.getLogById(id),
                "Lấy chi tiết nhật ký kiểm toán thành công"
        ));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("@perm.has('audit.view')")
    @RequirePermission("audit.view")
    @Operation(summary = "Lịch sử thay đổi của thực thể", description = "Lấy toàn bộ dòng lịch sử kiểm toán của một bản ghi nghiệp vụ cụ thể")
    public ResponseEntity<ApiResponse<PageData<AuditLogResponse>>> getLogsByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditLogService.getLogsByEntity(entityType, entityId, pageable),
                "Lấy lịch sử thay đổi thực thể thành công"
        ));
    }
}

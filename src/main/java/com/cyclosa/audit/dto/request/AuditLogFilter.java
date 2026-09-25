package com.cyclosa.audit.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Bộ lọc tra cứu nhật ký kiểm toán hệ thống")
public class AuditLogFilter extends BaseFilterRequest {

    @Parameter(description = "ID người thực hiện thao tác")
    private UUID userId;

    @Parameter(description = "Hành động thực hiện (CREATE, UPDATE, DELETE, APPROVE, REJECT...)")
    private String action;

    @Parameter(description = "Loại thực thể bị tác động (USER, ROLE, EMPLOYEE, CONTRACT, LEAVE...)")
    private String entityType;

    @Parameter(description = "ID thực thể bị tác động")
    private UUID entityId;

    @Parameter(description = "Từ ngày (YYYY-MM-DD)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @Parameter(description = "Đến ngày (YYYY-MM-DD)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}

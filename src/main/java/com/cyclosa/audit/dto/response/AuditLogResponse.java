package com.cyclosa.audit.dto.response;

import com.cyclosa.common.dto.summary.UserSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin tóm tắt nhật ký kiểm toán")
public class AuditLogResponse {

    @Schema(description = "ID bản ghi nhật ký")
    private UUID id;

    @Schema(description = "Thông tin người thực hiện")
    private UserSummary user;

    @Schema(description = "Hành động thực hiện", example = "UPDATE")
    private String action;

    @Schema(description = "Loại thực thể", example = "ROLE")
    private String entityType;

    @Schema(description = "ID thực thể bị tác động")
    private UUID entityId;

    @Schema(description = "Địa chỉ IP", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "Thời điểm thực hiện")
    private LocalDateTime createdAt;
}

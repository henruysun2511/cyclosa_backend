package com.cyclosa.role.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin vai trò trong danh sách")
public class RoleResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;

    @Schema(description = "Công ty sở hữu vai trò (null nếu là vai trò hệ thống)")
    private CompanySummary company;

    private boolean isSystemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

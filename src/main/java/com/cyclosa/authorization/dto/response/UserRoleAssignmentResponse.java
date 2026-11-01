package com.cyclosa.authorization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin gán vai trò của người dùng tại công ty")
public class UserRoleAssignmentResponse {

    @Schema(description = "ID bản ghi gán vai trò")
    private UUID id;

    @Schema(description = "ID người dùng")
    private UUID userId;

    @Schema(description = "ID vai trò")
    private UUID roleId;

    @Schema(description = "Mã vai trò", example = "MANAGER")
    private String roleCode;

    @Schema(description = "Tên vai trò", example = "Cán bộ Quản lý")
    private String roleName;

    @Schema(description = "ID công ty nơi vai trò có hiệu lực")
    private UUID companyId;

    @Schema(description = "Thời điểm gán vai trò")
    private LocalDateTime createdAt;
}


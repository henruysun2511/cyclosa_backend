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
@Schema(description = "Thông tin vai trò tóm tắt")
public class RoleResponse {

    @Schema(description = "ID vai trò")
    private UUID id;

    @Schema(description = "ID công ty sở hữu (NULL nếu là vai trò hệ thống)")
    private UUID companyId;

    @Schema(description = "Mã vai trò", example = "HR_ADMIN")
    private String code;

    @Schema(description = "Tên vai trò hiển thị", example = "Trưởng phòng Nhân sự")
    private String name;

    @Schema(description = "Mô tả vai trò")
    private String description;

    @Schema(description = "Cờ đánh dấu vai trò hệ thống không được xóa")
    private Boolean isSystemRole;

    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;
}


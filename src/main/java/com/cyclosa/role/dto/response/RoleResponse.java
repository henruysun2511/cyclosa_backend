package com.cyclosa.role.dto.response;

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
public class RoleResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private UUID companyId;
    private boolean isSystemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

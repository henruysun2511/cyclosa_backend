package com.cyclosa.role.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserRolesRequest {

    @NotEmpty(message = "Danh sách vai trò không được để trống")
    @Valid
    private List<UserRoleItem> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleItem {

        @NotNull(message = "ID vai trò không được để trống")
        private UUID roleId;

        private UUID companyId;
    }
}

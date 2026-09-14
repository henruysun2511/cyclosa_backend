package com.cyclosa.common.security;

import com.cyclosa.role.dto.response.EffectivePermissionResponse;
import com.cyclosa.role.service.UserRoleService;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component("perm")
@RequiredArgsConstructor
public class SecurityPermissionEvaluator {

    private final UserRoleService userRoleService;

    public boolean has(String permissionCode) {
        return has(permissionCode, DataScope.OWN);
    }

    public boolean has(String permissionCode, DataScope requiredScope) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return true;
        }

        Optional<UUID> userIdOpt = SecurityUtils.getCurrentUserIdOptional();
        if (userIdOpt.isEmpty()) {
            return false;
        }

        List<EffectivePermissionResponse> permissions = userRoleService.getEffectivePermissions(userIdOpt.get());
        return permissions.stream()
                .anyMatch(p -> p.getPermissionCode().equalsIgnoreCase(permissionCode)
                        && p.getDataScope().isBroaderOrEqualTo(requiredScope));
    }

    public Optional<DataScope> getDataScope(String permissionCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return Optional.of(DataScope.ALL);
        }

        Optional<UUID> userIdOpt = SecurityUtils.getCurrentUserIdOptional();
        if (userIdOpt.isEmpty()) {
            return Optional.empty();
        }

        List<EffectivePermissionResponse> permissions = userRoleService.getEffectivePermissions(userIdOpt.get());
        return permissions.stream()
                .filter(p -> p.getPermissionCode().equalsIgnoreCase(permissionCode))
                .map(EffectivePermissionResponse::getDataScope)
                .findFirst();
    }
}

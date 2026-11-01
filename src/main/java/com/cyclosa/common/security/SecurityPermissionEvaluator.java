package com.cyclosa.common.security;

import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.authorization.security.RoleCacheService;
import com.cyclosa.authorization.service.UserRoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Component thẩm định quyền chức năng và phạm vi dữ liệu trong SpEL (@PreAuthorize).
 * Cú pháp: @PreAuthorize("@perm.has('employee.view', T(com.cyclosa.common.enums.DataScope).DEPARTMENT)")
 */
@Slf4j
@Component("perm")
@RequiredArgsConstructor
public class SecurityPermissionEvaluator {

    private final RoleCacheService roleCacheService;
    private final UserRoleService userRoleService;

    public boolean has(String permissionCode) {
        return has(permissionCode, DataScope.OWN);
    }

    public boolean has(String permissionCode, DataScope requiredScope) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // 1. Kiểm tra Super Admin bypass
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));
        if (isSuperAdmin) {
            return true;
        }

        // 2. Lấy User ID
        Optional<UUID> userIdOpt = SecurityUtils.getCurrentUserIdOptional();
        if (userIdOpt.isEmpty()) {
            return false;
        }
        UUID userId = userIdOpt.get();

        // 3. Lấy Company ID từ Header (nếu có)
        UUID companyId = extractCurrentCompanyId();

        // 4. Lấy ma trận quyền hiệu lực
        Map<String, DataScope> effectivePerms = userRoleService.calculateEffectivePermissions(userId, companyId);
        DataScope userScope = effectivePerms.get(permissionCode);
        if (userScope == null) {
            log.debug("Người dùng userId={} không có quyền {}", userId, permissionCode);
            return false;
        }

        // 5. Kiểm tra cấp độ phạm vi dữ liệu: userScope >= requiredScope
        boolean hasScope = userScope.isBroaderOrEqualTo(requiredScope);
        if (!hasScope) {
            log.debug("Người dùng userId={} có quyền {} nhưng scope {} < required {}", userId, permissionCode, userScope, requiredScope);
        }
        return hasScope;
    }

    public Optional<DataScope> getDataScope(String permissionCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));
        if (isSuperAdmin) {
            return Optional.of(DataScope.ALL);
        }

        Optional<UUID> userIdOpt = SecurityUtils.getCurrentUserIdOptional();
        if (userIdOpt.isEmpty()) {
            return Optional.empty();
        }

        UUID companyId = extractCurrentCompanyId();
        Map<String, DataScope> effectivePerms = userRoleService.calculateEffectivePermissions(userIdOpt.get(), companyId);
        return Optional.ofNullable(effectivePerms.get(permissionCode));
    }

    private UUID extractCurrentCompanyId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String compHeader = request.getHeader("X-Company-Id");
                if (compHeader != null && !compHeader.isBlank()) {
                    return UUID.fromString(compHeader.trim());
                }
            }
        } catch (Exception e) {
            log.trace("Không thể lấy companyId từ request header: {}", e.getMessage());
        }
        return null;
    }
}

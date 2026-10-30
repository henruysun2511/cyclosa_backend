package com.cyclosa.common.security;

import com.cyclosa.common.enums.DataScope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("perm")
public class SecurityPermissionEvaluator {

    public boolean has(String permissionCode) {
        return has(permissionCode, DataScope.OWN);
    }

    public boolean has(String permissionCode, DataScope requiredScope) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }

    public Optional<DataScope> getDataScope(String permissionCode) {
        return Optional.of(DataScope.ALL);
    }
}

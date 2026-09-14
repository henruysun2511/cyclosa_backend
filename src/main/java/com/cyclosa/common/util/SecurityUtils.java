package com.cyclosa.common.util;

import com.cyclosa.common.exception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID currentUserId(Authentication auth) {
        if (auth == null || auth.getDetails() == null) {
            throw AppException.unauthorized();
        }
        try {
            return UUID.fromString(auth.getDetails().toString());
        } catch (IllegalArgumentException e) {
            throw AppException.unauthorized();
        }
    }

    public static Optional<UUID> getCurrentUserIdOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(auth.getDetails().toString()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

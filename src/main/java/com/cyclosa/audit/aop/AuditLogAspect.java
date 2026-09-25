package com.cyclosa.audit.aop;

import com.cyclosa.audit.service.AuditLogService;
import com.cyclosa.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserIdOptional().orElse(null);

            HttpServletRequest request = getHttpServletRequest();
            String ipAddress = getClientIp(request);
            String userAgent = request != null ? request.getHeader("User-Agent") : null;

            UUID entityId = extractEntityId(joinPoint.getArgs(), result);

            auditLogService.log(
                    currentUserId,
                    auditable.action(),
                    auditable.entityType(),
                    entityId,
                    null,
                    null,
                    ipAddress,
                    userAgent
            );
        } catch (Exception e) {
            log.warn("[AUDIT AOP] Không thể tự động ghi nhận audit log: {}", e.getMessage());
        }

        return result;
    }

    private UUID extractEntityId(Object[] args, Object result) {
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof UUID uuid) {
                    return uuid;
                }
            }
        }
        return null;
    }

    private HttpServletRequest getHttpServletRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

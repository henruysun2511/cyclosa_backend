package com.cyclosa.workplace.service;

import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkplaceSecurityContext {

    private final EmployeeService employeeService;

    public Optional<UUID> getCurrentUserId() {
        return SecurityUtils.getCurrentUserIdOptional();
    }

    public Optional<UUID> getCurrentEmployeeId() {
        return getCurrentUserId().flatMap(employeeService::findEmployeeIdByUserId);
    }

    public UUID getRequiredEmployeeId() {
        return getCurrentEmployeeId().orElse(null);
    }

    public UUID getCurrentCompanyId() {
        // 1. Thử lấy từ request header X-Company-Id
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String headerVal = request.getHeader("X-Company-Id");
            if (headerVal != null && !headerVal.isBlank()) {
                try {
                    return UUID.fromString(headerVal.trim());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // 2. Tra cứu từ thông tin nhân viên của User hiện tại
        Optional<UUID> empIdOpt = getCurrentEmployeeId();
        if (empIdOpt.isPresent()) {
            try {
                EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(empIdOpt.get());
                if (emp != null && emp.getCompany() != null && emp.getCompany().getId() != null) {
                    return emp.getCompany().getId();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}

package com.cyclosa.offboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.offboarding.dto.response.OffboardingSummaryResponse;
import com.cyclosa.offboarding.service.OffboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offboarding")
@RequiredArgsConstructor
@Tag(name = "Offboarding Orchestration", description = "Trung tâm điều phối tổng hợp tiến trình thôi việc, kiểm tra tài sản/phép tồn và hoàn tất thôi việc")
public class OffboardingProcessController {

    private final OffboardingService offboardingService;

    @GetMapping("/summary/{employeeId}")
    @PreAuthorize("@perm.has('offboarding.view') or @perm.has('offboarding.view_own')")
    @RequirePermission("offboarding.view")
    @Operation(summary = "Xem bảng tổng hợp tiến trình thôi việc (hồ sơ, checklist bàn giao, tài sản chưa trả, phép tồn)")
    public ResponseEntity<ApiResponse<OffboardingSummaryResponse>> getOffboardingSummary(
            @PathVariable UUID employeeId
    ) {
        OffboardingSummaryResponse response = offboardingService.getOffboardingSummary(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy bảng tổng hợp thôi việc thành công"));
    }

    @PutMapping("/complete/{employeeId}")
    @PreAuthorize("@perm.has('offboarding.manage')")
    @RequirePermission("offboarding.manage")
    @Operation(summary = "Chính thức hoàn tất thôi việc (kiểm tra 100% bàn giao, thu hồi tài sản, chuyển trạng thái nhân viên và vô hiệu hóa tài khoản)")
    public ResponseEntity<ApiResponse<OffboardingSummaryResponse>> completeOffboarding(
            @PathVariable UUID employeeId
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        OffboardingSummaryResponse response = offboardingService.completeOffboarding(employeeId, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Hoàn tất quy trình thôi việc thành công"));
    }
}

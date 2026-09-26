package com.cyclosa.offboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.offboarding.dto.request.UpdateClearanceRequest;
import com.cyclosa.offboarding.dto.response.OffboardingClearanceResponse;
import com.cyclosa.offboarding.service.OffboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offboarding/clearances")
@RequiredArgsConstructor
@Tag(name = "Offboarding - Clearances", description = "Quản lý thủ tục bàn giao tài sản, tài chính, IT và phòng ban trước khi nghỉ việc")
public class OffboardingClearanceController {

    private final OffboardingService offboardingService;

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@perm.has('offboarding.view') or @perm.has('offboarding.view_own')")
    @RequirePermission("offboarding.view")
    @Operation(summary = "Lấy danh sách các hạng mục bàn giao của nhân viên")
    public ResponseEntity<ApiResponse<List<OffboardingClearanceResponse>>> getClearances(
            @PathVariable UUID employeeId
    ) {
        List<OffboardingClearanceResponse> response = offboardingService.getClearances(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách bàn giao thành công"));
    }

    @PutMapping
    @PreAuthorize("@perm.has('offboarding.approve') or @perm.has('offboarding.manage')")
    @RequirePermission("offboarding.approve")
    @Operation(summary = "Xác nhận bàn giao hạng mục (kiểm tra thu hồi tài sản nếu là mục ASSET)")
    public ResponseEntity<ApiResponse<OffboardingClearanceResponse>> updateClearance(
            @Valid @RequestBody UpdateClearanceRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        OffboardingClearanceResponse response = offboardingService.updateClearance(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật xác nhận bàn giao thành công"));
    }
}

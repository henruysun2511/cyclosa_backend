package com.cyclosa.onboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.onboarding.dto.filter.OnboardingProcessFilter;
import com.cyclosa.onboarding.dto.request.*;
import com.cyclosa.onboarding.dto.response.*;
import com.cyclosa.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding Process Management", description = "Quản lý toàn bộ tiến trình tiếp nhận nhân sự mới, bàn giao checklist, tài khoản và đào tạo định hướng")
public class OnboardingController {

    private final OnboardingService onboardingService;

    // =========================================================================
    // 1. ONBOARDING PROCESSES
    // =========================================================================

    @PostMapping("/processes")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Khởi tạo tiến trình Onboarding cho nhân viên mới")
    public ResponseEntity<ApiResponse<OnboardingProcessDetailResponse>> createProcess(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateOnboardingProcessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                onboardingService.createProcess(headerCompanyId, request),
                "Khởi tạo quy trình Onboarding thành công"));
    }

    @GetMapping("/processes")
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Danh sách các tiến trình Onboarding đang quản lý")
    public ResponseEntity<ApiResponse<PageData<OnboardingProcessResponse>>> getProcesses(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute OnboardingProcessFilter filter) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getProcesses(headerCompanyId, filter),
                "Lấy danh sách tiến trình Onboarding thành công"));
    }

    @GetMapping("/processes/my")
    @PreAuthorize("@perm.has('onboarding.view_own')")
    @RequirePermission("onboarding.view_own")
    @Operation(summary = "Nhân viên tự xem tiến trình Onboarding của chính mình (ESS)")
    public ResponseEntity<ApiResponse<OnboardingProcessDetailResponse>> getMyProcess() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getMyProcess(currentUserId),
                "Lấy thông tin tiến trình Onboarding của bạn thành công"));
    }

    @GetMapping("/processes/employee/{employeeId}")
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Xem tiến trình Onboarding theo ID nhân viên")
    public ResponseEntity<ApiResponse<OnboardingProcessDetailResponse>> getProcessByEmployeeId(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getProcessByEmployeeId(headerCompanyId, employeeId),
                "Lấy tiến trình Onboarding theo nhân viên thành công"));
    }

    @GetMapping("/processes/{id}")
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Xem chi tiết toàn diện một tiến trình Onboarding")
    public ResponseEntity<ApiResponse<OnboardingProcessDetailResponse>> getProcessById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getProcessById(headerCompanyId, id),
                "Lấy chi tiết tiến trình Onboarding thành công"));
    }

    @GetMapping("/processes/{id}/progress")
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Xem thống kê tiến độ hoàn thành Onboarding")
    public ResponseEntity<ApiResponse<OnboardingProgressResponse>> getProgress(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getProgress(headerCompanyId, id),
                "Lấy thống kê tiến độ Onboarding thành công"));
    }

    @PutMapping("/processes/{id}/complete")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Xác nhận hoàn tất 100% Onboarding và chuyển nhân viên sang trạng thái Chính thức (ACTIVE)")
    public ResponseEntity<ApiResponse<OnboardingProcessDetailResponse>> completeProcess(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.completeProcess(headerCompanyId, id, currentUserId),
                "Hoàn tất quy trình Onboarding và kích hoạt nhân viên chính thức thành công"));
    }

    @PutMapping("/processes/{id}/cancel")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Hủy quy trình Onboarding")
    public ResponseEntity<ApiResponse<OnboardingProcessDetailResponse>> cancelProcess(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.cancelProcess(headerCompanyId, id, reason),
                "Hủy quy trình Onboarding thành công"));
    }

    // =========================================================================
    // 2. PROCESS ITEMS
    // =========================================================================

    @PutMapping("/process-items/{id}/complete")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Đánh dấu hoàn thành một hạng mục Onboarding")
    public ResponseEntity<ApiResponse<OnboardingProcessItemResponse>> completeProcessItem(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CompleteProcessItemRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.completeProcessItem(headerCompanyId, id, request, currentUserId),
                "Đánh dấu hoàn thành hạng mục thành công"));
    }

    @PutMapping("/process-items/{id}/skip")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Bỏ qua một hạng mục Onboarding")
    public ResponseEntity<ApiResponse<OnboardingProcessItemResponse>> skipProcessItem(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.skipProcessItem(headerCompanyId, id, note, currentUserId),
                "Đã bỏ qua hạng mục Onboarding"));
    }

    // =========================================================================
    // 3. ACCOUNT PROVISIONING
    // =========================================================================

    @PostMapping("/processes/{id}/account-provisioning")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Tạo yêu cầu cấp phát tài khoản hệ thống cho nhân viên")
    public ResponseEntity<ApiResponse<AccountProvisioningResponse>> createAccountProvisioning(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateAccountProvisioningRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                onboardingService.createAccountProvisioning(headerCompanyId, id, request, currentUserId),
                "Tạo yêu cầu cấp phát tài khoản thành công"));
    }

    @GetMapping("/processes/{id}/account-provisioning")
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Danh sách tài khoản hệ thống của tiến trình Onboarding")
    public ResponseEntity<ApiResponse<List<AccountProvisioningResponse>>> getAccountsByProcessId(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getAccountsByProcessId(headerCompanyId, id),
                "Lấy danh sách tài khoản cấp phát thành công"));
    }

    @PutMapping("/account-provisioning/{accountId}/status")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Cập nhật trạng thái cấp phát tài khoản (PROVISIONED / REVOKED)")
    public ResponseEntity<ApiResponse<AccountProvisioningResponse>> updateAccountProvisioningStatus(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountProvisioningRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.updateAccountProvisioningStatus(headerCompanyId, accountId, request, currentUserId),
                "Cập nhật trạng thái cấp phát tài khoản thành công"));
    }

    // =========================================================================
    // 4. ORIENTATION SESSIONS
    // =========================================================================

    @PostMapping("/processes/{id}/orientation-sessions")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Lên lịch buổi đào tạo hội nhập / định hướng cho nhân sự")
    public ResponseEntity<ApiResponse<OrientationSessionResponse>> createOrientationSession(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateOrientationSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                onboardingService.createOrientationSession(headerCompanyId, id, request),
                "Lên lịch buổi đào tạo hội nhập thành công"));
    }

    @GetMapping("/processes/{id}/orientation-sessions")
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Danh sách các buổi đào tạo định hướng của quy trình Onboarding")
    public ResponseEntity<ApiResponse<List<OrientationSessionResponse>>> getOrientationSessionsByProcessId(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getOrientationSessionsByProcessId(headerCompanyId, id),
                "Lấy danh sách buổi định hướng thành công"));
    }

    @PutMapping("/orientation-sessions/{sessionId}")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Cập nhật thông tin hoặc trạng thái buổi đào tạo định hướng")
    public ResponseEntity<ApiResponse<OrientationSessionResponse>> updateOrientationSession(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateOrientationSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.updateOrientationSession(headerCompanyId, sessionId, request),
                "Cập nhật buổi đào tạo định hướng thành công"));
    }
}

package com.cyclosa.onboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.onboarding.dto.request.UploadEmployeeDocumentRequest;
import com.cyclosa.onboarding.dto.response.EmployeeDocumentResponse;
import com.cyclosa.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Documents", description = "Quản lý tài liệu, hồ sơ số hóa nhân sự (CCCD, Bằng cấp, Chứng chỉ, Hợp đồng)")
public class EmployeeDocumentController {

    private final OnboardingService onboardingService;

    @PostMapping("/{id}/documents")
    @PreAuthorize("@perm.has('employee.update') or @perm.has('onboarding.manage')")
    @RequirePermission("employee.update")
    @Operation(summary = "Tải lên tài liệu hồ sơ nhân viên (CCCD, bằng cấp, chứng chỉ...)")
    public ResponseEntity<ApiResponse<EmployeeDocumentResponse>> uploadDocument(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UploadEmployeeDocumentRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                onboardingService.uploadDocument(headerCompanyId, id, request, currentUserId),
                "Tải lên tài liệu hồ sơ thành công"));
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("@perm.has('employee.view') or @perm.has('onboarding.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Lấy danh sách tài liệu hồ sơ của nhân viên")
    public ResponseEntity<ApiResponse<List<EmployeeDocumentResponse>>> getDocuments(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getDocumentsByEmployeeId(headerCompanyId, id),
                "Lấy danh sách tài liệu thành công"));
    }

    @DeleteMapping("/documents/{docId}")
    @PreAuthorize("@perm.has('employee.update') or @perm.has('onboarding.manage')")
    @RequirePermission("employee.update")
    @Operation(summary = "Xóa tài liệu hồ sơ nhân viên")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID docId) {
        onboardingService.deleteDocument(headerCompanyId, docId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa tài liệu thành công"));
    }

    @PutMapping("/documents/{docId}/verify")
    @PreAuthorize("@perm.has('employee.update') or @perm.has('onboarding.manage')")
    @RequirePermission("employee.update")
    @Operation(summary = "Xác thực / Duyệt tính hợp lệ của tài liệu hồ sơ nhân sự")
    public ResponseEntity<ApiResponse<EmployeeDocumentResponse>> verifyDocument(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID docId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.verifyDocument(headerCompanyId, docId, currentUserId),
                "Xác thực tài liệu hồ sơ thành công"));
    }
}

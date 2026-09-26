package com.cyclosa.offboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.offboarding.dto.request.ApplyResignationRequest;
import com.cyclosa.offboarding.dto.request.ApproveResignationRequest;
import com.cyclosa.offboarding.dto.request.ResignationFilter;
import com.cyclosa.offboarding.dto.response.ResignationDetailResponse;
import com.cyclosa.offboarding.dto.response.ResignationResponse;
import com.cyclosa.offboarding.service.OffboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offboarding/resignations")
@RequiredArgsConstructor
@Tag(name = "Offboarding - Resignations", description = "Quản lý đơn xin thôi việc tự nguyện của nhân viên")
public class ResignationController {

    private final OffboardingService offboardingService;

    @PostMapping
    @PreAuthorize("@perm.has('offboarding.view_own')")
    @RequirePermission("offboarding.view_own")
    @Operation(summary = "Nhân viên nộp đơn xin thôi việc")
    public ResponseEntity<ApiResponse<ResignationResponse>> applyResignation(
            @Valid @RequestBody ApplyResignationRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ResignationResponse response = offboardingService.applyResignation(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Nộp đơn xin thôi việc thành công"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("@perm.has('offboarding.approve')")
    @RequirePermission("offboarding.approve")
    @Operation(summary = "Quản lý / HR duyệt hoặc từ chối đơn xin thôi việc")
    public ResponseEntity<ApiResponse<ResignationResponse>> approveResignation(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveResignationRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ResignationResponse response = offboardingService.approveResignation(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Xử lý duyệt đơn thôi việc thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('offboarding.view') or @perm.has('offboarding.view_own')")
    @RequirePermission("offboarding.view")
    @Operation(summary = "Xem chi tiết đơn xin thôi việc")
    public ResponseEntity<ApiResponse<ResignationDetailResponse>> getResignationById(@PathVariable UUID id) {
        ResignationDetailResponse response = offboardingService.getResignationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin đơn thôi việc thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('offboarding.view')")
    @RequirePermission("offboarding.view")
    @Operation(summary = "Danh sách đơn xin thôi việc có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<ResignationResponse>>> getResignations(
            @Valid @ModelAttribute ResignationFilter filter
    ) {
        PageData<ResignationResponse> response = offboardingService.getResignations(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách đơn thôi việc thành công"));
    }
}

package com.cyclosa.offboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.offboarding.dto.request.CreateTerminationRequest;
import com.cyclosa.offboarding.dto.request.TerminationFilter;
import com.cyclosa.offboarding.dto.response.TerminationDetailResponse;
import com.cyclosa.offboarding.dto.response.TerminationResponse;
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
@RequestMapping("/api/v1/offboarding/terminations")
@RequiredArgsConstructor
@Tag(name = "Offboarding - Terminations", description = "Quản lý quyết định chấm dứt hợp đồng lao động do công ty ban hành")
public class TerminationController {

    private final OffboardingService offboardingService;

    @PostMapping
    @PreAuthorize("@perm.has('offboarding.manage')")
    @RequirePermission("offboarding.manage")
    @Operation(summary = "Ban hành quyết định chấm dứt hợp đồng lao động (sa thải / tinh giản / hết hạn)")
    public ResponseEntity<ApiResponse<TerminationResponse>> createTermination(
            @Valid @RequestBody CreateTerminationRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        TerminationResponse response = offboardingService.createTermination(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Ban hành quyết định chấm dứt hợp đồng thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('offboarding.view')")
    @RequirePermission("offboarding.view")
    @Operation(summary = "Xem chi tiết quyết định chấm dứt hợp đồng")
    public ResponseEntity<ApiResponse<TerminationDetailResponse>> getTerminationById(@PathVariable UUID id) {
        TerminationDetailResponse response = offboardingService.getTerminationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin quyết định chấm dứt thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('offboarding.view')")
    @RequirePermission("offboarding.view")
    @Operation(summary = "Danh sách quyết định chấm dứt hợp đồng có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<TerminationResponse>>> getTerminations(
            @Valid @ModelAttribute TerminationFilter filter
    ) {
        PageData<TerminationResponse> response = offboardingService.getTerminations(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách quyết định chấm dứt thành công"));
    }
}

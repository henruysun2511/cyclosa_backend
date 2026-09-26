package com.cyclosa.onboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.onboarding.dto.filter.ChecklistTemplateFilter;
import com.cyclosa.onboarding.dto.request.CreateChecklistTemplateRequest;
import com.cyclosa.onboarding.dto.request.CreateTemplateItemRequest;
import com.cyclosa.onboarding.dto.request.UpdateChecklistTemplateRequest;
import com.cyclosa.onboarding.dto.request.UpdateTemplateItemRequest;
import com.cyclosa.onboarding.dto.response.ChecklistTemplateDetailResponse;
import com.cyclosa.onboarding.dto.response.ChecklistTemplateItemResponse;
import com.cyclosa.onboarding.dto.response.ChecklistTemplateResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding/checklist-templates")
@RequiredArgsConstructor
@Tag(name = "Onboarding Checklist Templates", description = "Quản lý mẫu danh sách công việc chuẩn bị cho nhân sự mới")
public class OnboardingChecklistController {

    private final OnboardingService onboardingService;

    @PostMapping
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Tạo mới mẫu danh sách checklist Onboarding")
    public ResponseEntity<ApiResponse<ChecklistTemplateDetailResponse>> createTemplate(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateChecklistTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                onboardingService.createTemplate(headerCompanyId, request),
                "Tạo mẫu checklist Onboarding thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Lấy danh sách các mẫu checklist Onboarding")
    public ResponseEntity<ApiResponse<PageData<ChecklistTemplateResponse>>> getTemplates(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute ChecklistTemplateFilter filter) {
        return ResponseEntity.ok(ApiResponse.ok(
            onboardingService.getTemplates(headerCompanyId, filter),
            "Lấy danh sách mẫu checklist thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('onboarding.view')")
    @RequirePermission("onboarding.view")
    @Operation(summary = "Xem chi tiết mẫu checklist Onboarding kèm các hạng mục")
    public ResponseEntity<ApiResponse<ChecklistTemplateDetailResponse>> getTemplateById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.getTemplateById(headerCompanyId, id),
                "Lấy chi tiết mẫu checklist thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Cập nhật mẫu checklist Onboarding")
    public ResponseEntity<ApiResponse<ChecklistTemplateDetailResponse>> updateTemplate(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateChecklistTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.updateTemplate(headerCompanyId, id, request),
                "Cập nhật mẫu checklist thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Xóa mẫu checklist Onboarding")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        onboardingService.deleteTemplate(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa mẫu checklist thành công"));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Thêm hạng mục công việc vào mẫu checklist")
    public ResponseEntity<ApiResponse<ChecklistTemplateItemResponse>> addItemToTemplate(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateTemplateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                onboardingService.addItemToTemplate(headerCompanyId, id, request),
                "Thêm hạng mục vào mẫu checklist thành công"));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Cập nhật hạng mục công việc trong mẫu checklist")
    public ResponseEntity<ApiResponse<ChecklistTemplateItemResponse>> updateTemplateItem(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateTemplateItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                onboardingService.updateTemplateItem(headerCompanyId, itemId, request),
                "Cập nhật hạng mục thành công"));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("@perm.has('onboarding.manage')")
    @RequirePermission("onboarding.manage")
    @Operation(summary = "Xóa hạng mục công việc khỏi mẫu checklist")
    public ResponseEntity<ApiResponse<Void>> deleteTemplateItem(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID itemId) {
        onboardingService.deleteTemplateItem(headerCompanyId, itemId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa hạng mục thành công"));
    }
}

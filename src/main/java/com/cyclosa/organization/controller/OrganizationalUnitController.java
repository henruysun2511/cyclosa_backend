package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.request.MoveOrgUnitRequest;
import com.cyclosa.organization.dto.request.UpdateOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitImpactPreviewResponse;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;
import com.cyclosa.organization.service.OrganizationalUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/organizational-units")
@RequiredArgsConstructor
@Tag(name = "Organizational Units", description = "Quản lý cơ cấu phòng ban và tái cơ cấu tổ chức")
public class OrganizationalUnitController {

    private final OrganizationalUnitService orgUnitService;

    @GetMapping("/tree")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy toàn bộ cây sơ đồ phòng ban phân cấp theo công ty")
    public ResponseEntity<ApiResponse<List<OrgUnitTreeResponse>>> getUnitTree(
            @Parameter(description = "ID công ty")
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                orgUnitService.getUnitTree(effectiveCompanyId),
                "Lấy cây sơ đồ phòng ban thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy chi tiết thông tin phòng ban theo ID")
    public ResponseEntity<ApiResponse<OrgUnitResponse>> getUnitById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orgUnitService.getUnitById(headerCompanyId, id),
                "Lấy thông tin phòng ban thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('organization.create')")
    @RequirePermission("organization.create")
    @Operation(summary = "Tạo mới phòng ban / đơn vị tổ chức")
    public ResponseEntity<ApiResponse<OrgUnitResponse>> createUnit(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateOrgUnitRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        orgUnitService.createUnit(headerCompanyId, request),
                        "Tạo mới phòng ban thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('organization.update')")
    @RequirePermission("organization.update")
    @Operation(summary = "Cập nhật thông tin phòng ban")
    public ResponseEntity<ApiResponse<OrgUnitResponse>> updateUnit(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdateOrgUnitRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orgUnitService.updateUnit(headerCompanyId, id, request),
                "Cập nhật phòng ban thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('organization.delete')")
    @RequirePermission("organization.delete")
    @Operation(summary = "Xóa phòng ban (ràng buộc không có đơn vị con trực thuộc)")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        orgUnitService.deleteUnit(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa phòng ban thành công"));
    }

    @GetMapping("/{id}/impact-preview")
    @PreAuthorize("@perm.has('organization.manage')")
    @RequirePermission("organization.manage")
    @Operation(summary = "Dự báo tác động trước khi điều chuyển cây tổ chức")
    public ResponseEntity<ApiResponse<OrgUnitImpactPreviewResponse>> getImpactPreview(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID targetParentId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orgUnitService.getImpactPreview(headerCompanyId, id, targetParentId),
                "Dự báo tác động thành công"));
    }

    @PutMapping("/{id}/move")
    @PreAuthorize("@perm.has('organization.manage')")
    @RequirePermission("organization.manage")
    @Operation(summary = "Điều chuyển phòng ban sang nhánh cha mới (Tái cơ cấu)")
    public ResponseEntity<ApiResponse<OrgUnitResponse>> moveUnit(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody MoveOrgUnitRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orgUnitService.moveUnit(headerCompanyId, id, request),
                "Điều chuyển phòng ban thành công"));
    }
}

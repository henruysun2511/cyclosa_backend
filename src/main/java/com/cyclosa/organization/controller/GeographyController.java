package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.dto.response.RegionResponse;
import com.cyclosa.organization.service.GeographyService;
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
@RequestMapping("/api/v1/geography")
@RequiredArgsConstructor
@Tag(name = "Geography", description = "Quản lý vùng miền và chi nhánh làm việc")
public class GeographyController {

    private final GeographyService geographyService;

    @GetMapping("/tree")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy cây địa lý vùng miền kèm các chi nhánh trực thuộc")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getGeographyTree(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                geographyService.getGeographyTree(effectiveCompanyId),
                "Lấy sơ đồ địa lý thành công"));
    }

    @PostMapping("/regions")
    @PreAuthorize("@perm.has('organization.manage')")
    @RequirePermission("organization.manage")
    @Operation(summary = "Tạo mới vùng miền")
    public ResponseEntity<ApiResponse<RegionResponse>> createRegion(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateRegionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        geographyService.createRegion(headerCompanyId, request),
                        "Tạo vùng miền thành công"));
    }

    @GetMapping("/branches")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Danh sách toàn bộ chi nhánh của công ty")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                geographyService.getBranches(effectiveCompanyId),
                "Lấy danh sách chi nhánh thành công"));
    }

    @GetMapping("/branches/{id}")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy chi tiết thông tin chi nhánh")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                geographyService.getBranchById(headerCompanyId, id),
                "Lấy thông tin chi nhánh thành công"));
    }

    @PostMapping("/branches")
    @PreAuthorize("@perm.has('organization.create')")
    @RequirePermission("organization.create")
    @Operation(summary = "Tạo mới chi nhánh làm việc (tọa độ GPS & bán kính)")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateBranchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        geographyService.createBranch(headerCompanyId, request),
                        "Tạo chi nhánh thành công"));
    }

    @PutMapping("/branches/{id}")
    @PreAuthorize("@perm.has('organization.update')")
    @RequirePermission("organization.update")
    @Operation(summary = "Cập nhật thông tin chi nhánh")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdateBranchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                geographyService.updateBranch(headerCompanyId, id, request),
                "Cập nhật chi nhánh thành công"));
    }
}

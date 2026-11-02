package com.cyclosa.organization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.UpdateRegionRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.dto.response.RegionResponse;
import com.cyclosa.organization.service.BranchService;
import com.cyclosa.organization.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
@Tag(name = "01. Cơ cấu tổ chức - Khu vực & Vùng miền", description = "APIs quản lý phân vùng địa lý")
public class RegionController {

    private final RegionService regionService;
    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "Lấy danh sách khu vực theo công ty có phân trang")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<PageData<RegionResponse>> getRegions(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Từ khóa tìm kiếm")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(regionService.getRegions(companyId, keyword, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ danh sách khu vực theo công ty (không phân trang)")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<RegionResponse>> getAllRegions(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        return ApiResponse.ok(regionService.getAllRegionsByCompany(companyId));
    }

    @GetMapping("/{regionId}")
    @Operation(summary = "Lấy thông tin chi tiết một khu vực theo ID")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<RegionResponse> getRegionById(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId) {
        return ApiResponse.ok(regionService.getRegionById(regionId));
    }

    @GetMapping("/{regionId}/branches")
    @Operation(summary = "Lấy danh sách các chi nhánh trực thuộc một khu vực")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<BranchResponse>> getBranchesByRegion(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId) {
        return ApiResponse.ok(branchService.getBranchesByRegion(regionId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới khu vực / vùng miền")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<RegionResponse> createRegion(@Valid @RequestBody CreateRegionRequest request) {
        return ApiResponse.created(regionService.createRegion(request));
    }

    @PutMapping("/{regionId}")
    @Operation(summary = "Cập nhật thông tin khu vực")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<RegionResponse> updateRegion(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId,
            @Valid @RequestBody UpdateRegionRequest request) {
        return ApiResponse.ok(regionService.updateRegion(regionId, request));
    }

    @DeleteMapping("/{regionId}")
    @Operation(summary = "Xóa mềm khu vực")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> deleteRegion(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId) {
        regionService.deleteRegion(regionId);
        return ApiResponse.ok(null);
    }
}


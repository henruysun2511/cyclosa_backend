package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.RegionFilterRequest;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
@Tag(name = "Regions", description = "Quản lý phân vùng địa lý")
public class RegionController {

    private final RegionService regionService;
    private final BranchService branchService;

    @GetMapping
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách khu vực theo công ty có phân trang", description = "Hỗ trợ lọc từ khóa và phân trang")
    public ResponseEntity<ApiResponse<PageData<RegionResponse>>> getRegions(@ParameterObject @Valid RegionFilterRequest filter) {
        PageData<RegionResponse> result = regionService.getRegions(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách khu vực thành công"));
    }

    @GetMapping("/all")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy toàn bộ danh sách khu vực theo công ty (không phân trang)")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getAllRegions(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        List<RegionResponse> result = regionService.getAllRegionsByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy toàn bộ danh sách khu vực thành công"));
    }

    @GetMapping("/{regionId}")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy thông tin chi tiết một khu vực theo ID")
    public ResponseEntity<ApiResponse<RegionResponse>> getRegionById(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId) {
        RegionResponse result = regionService.getRegionById(regionId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin khu vực thành công"));
    }

    @GetMapping("/{regionId}/branches")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách các chi nhánh trực thuộc một khu vực")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranchesByRegion(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId) {
        List<BranchResponse> result = branchService.getBranchesByRegion(regionId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách chi nhánh theo khu vực thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Tạo mới khu vực / vùng miền")
    public ResponseEntity<ApiResponse<RegionResponse>> createRegion(@Valid @RequestBody CreateRegionRequest request) {
        RegionResponse result = regionService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mới khu vực thành công"));
    }

    @PutMapping("/{regionId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Cập nhật thông tin khu vực")
    public ResponseEntity<ApiResponse<RegionResponse>> updateRegion(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId,
            @Valid @RequestBody UpdateRegionRequest request) {
        RegionResponse result = regionService.updateRegion(regionId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật khu vực thành công"));
    }

    @DeleteMapping("/{regionId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Xóa mềm khu vực")
    public ResponseEntity<ApiResponse<Void>> deleteRegion(
            @Parameter(description = "ID khu vực", required = true)
            @PathVariable UUID regionId) {
        regionService.deleteRegion(regionId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa khu vực thành công"));
    }
}

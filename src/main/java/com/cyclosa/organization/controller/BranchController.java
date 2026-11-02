package com.cyclosa.organization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.service.BranchService;
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
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "01. Cơ cấu tổ chức - Chi nhánh & Văn phòng", description = "APIs quản lý văn phòng làm việc và tọa độ GPS chấm công")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "Lấy danh sách chi nhánh theo khu vực có phân trang và bộ lọc")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<PageData<BranchResponse>> getBranches(
            @Parameter(description = "ID khu vực trực thuộc", required = true)
            @RequestParam UUID regionId,
            @Parameter(description = "Từ khóa tìm kiếm")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái hoạt động")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(branchService.getBranches(regionId, keyword, isActive, pageable));
    }

    @GetMapping("/by-company")
    @Operation(summary = "Lấy toàn bộ chi nhánh theo công ty (cho chọn nhanh / chấm công GPS)")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<BranchResponse>> getBranchesByCompany(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        return ApiResponse.ok(branchService.getBranchesByCompany(companyId));
    }

    @GetMapping("/{branchId}")
    @Operation(summary = "Lấy thông tin chi tiết một chi nhánh theo ID")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<BranchResponse> getBranchById(
            @Parameter(description = "ID chi nhánh", required = true)
            @PathVariable UUID branchId) {
        return ApiResponse.ok(branchService.getBranchById(branchId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới chi nhánh / văn phòng làm việc")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<BranchResponse> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        return ApiResponse.created(branchService.createBranch(request));
    }

    @PutMapping("/{branchId}")
    @Operation(summary = "Cập nhật thông tin chi nhánh / tọa độ GPS")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<BranchResponse> updateBranch(
            @Parameter(description = "ID chi nhánh", required = true)
            @PathVariable UUID branchId,
            @Valid @RequestBody UpdateBranchRequest request) {
        return ApiResponse.ok(branchService.updateBranch(branchId, request));
    }

    @DeleteMapping("/{branchId}")
    @Operation(summary = "Xóa mềm chi nhánh")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> deleteBranch(
            @Parameter(description = "ID chi nhánh", required = true)
            @PathVariable UUID branchId) {
        branchService.deleteBranch(branchId);
        return ApiResponse.ok(null);
    }
}


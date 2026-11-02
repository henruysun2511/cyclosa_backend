package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.BranchFilterRequest;
import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.service.BranchService;
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
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Quản lý văn phòng làm việc và tọa độ GPS chấm công")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách chi nhánh theo khu vực có phân trang và bộ lọc", description = "Hỗ trợ lọc từ khóa, trạng thái hoạt động và phân trang")
    public ResponseEntity<ApiResponse<PageData<BranchResponse>>> getBranches(@ParameterObject @Valid BranchFilterRequest filter) {
        PageData<BranchResponse> result = branchService.getBranches(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách chi nhánh thành công"));
    }

    @GetMapping("/by-company")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy toàn bộ chi nhánh theo công ty (cho chọn nhanh / chấm công GPS)")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranchesByCompany(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        List<BranchResponse> result = branchService.getBranchesByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách chi nhánh theo công ty thành công"));
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy thông tin chi tiết một chi nhánh theo ID")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @Parameter(description = "ID chi nhánh", required = true)
            @PathVariable UUID branchId) {
        BranchResponse result = branchService.getBranchById(branchId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin chi nhánh thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Tạo mới chi nhánh / văn phòng làm việc")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        BranchResponse result = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mới chi nhánh thành công"));
    }

    @PutMapping("/{branchId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Cập nhật thông tin chi nhánh / tọa độ GPS")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @Parameter(description = "ID chi nhánh", required = true)
            @PathVariable UUID branchId,
            @Valid @RequestBody UpdateBranchRequest request) {
        BranchResponse result = branchService.updateBranch(branchId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật thông tin chi nhánh thành công"));
    }

    @DeleteMapping("/{branchId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Xóa mềm chi nhánh")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(
            @Parameter(description = "ID chi nhánh", required = true)
            @PathVariable UUID branchId) {
        branchService.deleteBranch(branchId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa chi nhánh thành công"));
    }
}

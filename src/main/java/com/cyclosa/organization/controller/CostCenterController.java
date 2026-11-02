package com.cyclosa.organization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.request.UpdateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.service.CostCenterService;
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
@RequestMapping("/api/v1/cost-centers")
@RequiredArgsConstructor
@Tag(name = "01. Cơ cấu tổ chức - Trung tâm chi phí", description = "APIs quản lý danh mục trung tâm chi phí hạch toán")
public class CostCenterController {

    private final CostCenterService costCenterService;

    @GetMapping
    @Operation(summary = "Lấy danh sách trung tâm chi phí theo công ty có phân trang")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<PageData<CostCenterResponse>> getCostCenters(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Từ khóa tìm kiếm")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(costCenterService.getCostCenters(companyId, keyword, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ trung tâm chi phí theo công ty (cho dropdown)")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<CostCenterResponse>> getAllCostCenters(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        return ApiResponse.ok(costCenterService.getAllCostCentersByCompany(companyId));
    }

    @GetMapping("/{costCenterId}")
    @Operation(summary = "Lấy thông tin chi tiết một trung tâm chi phí theo ID")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<CostCenterResponse> getCostCenterById(
            @Parameter(description = "ID trung tâm chi phí", required = true)
            @PathVariable UUID costCenterId) {
        return ApiResponse.ok(costCenterService.getCostCenterById(costCenterId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới trung tâm chi phí")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<CostCenterResponse> createCostCenter(@Valid @RequestBody CreateCostCenterRequest request) {
        return ApiResponse.created(costCenterService.createCostCenter(request));
    }

    @PutMapping("/{costCenterId}")
    @Operation(summary = "Cập nhật thông tin trung tâm chi phí")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<CostCenterResponse> updateCostCenter(
            @Parameter(description = "ID trung tâm chi phí", required = true)
            @PathVariable UUID costCenterId,
            @Valid @RequestBody UpdateCostCenterRequest request) {
        return ApiResponse.ok(costCenterService.updateCostCenter(costCenterId, request));
    }

    @DeleteMapping("/{costCenterId}")
    @Operation(summary = "Xóa mềm trung tâm chi phí")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> deleteCostCenter(
            @Parameter(description = "ID trung tâm chi phí", required = true)
            @PathVariable UUID costCenterId) {
        costCenterService.deleteCostCenter(costCenterId);
        return ApiResponse.ok(null);
    }
}


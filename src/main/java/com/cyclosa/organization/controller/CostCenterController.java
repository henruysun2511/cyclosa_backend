package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CostCenterFilterRequest;
import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.request.UpdateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.service.CostCenterService;
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
@RequestMapping("/api/v1/cost-centers")
@RequiredArgsConstructor
@Tag(name = "Cost Centers", description = "Quản lý danh mục trung tâm chi phí hạch toán")
public class CostCenterController {

    private final CostCenterService costCenterService;

    @GetMapping
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách trung tâm chi phí theo công ty có phân trang", description = "Hỗ trợ lọc từ khóa và phân trang")
    public ResponseEntity<ApiResponse<PageData<CostCenterResponse>>> getCostCenters(@ParameterObject @Valid CostCenterFilterRequest filter) {
        PageData<CostCenterResponse> result = costCenterService.getCostCenters(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách trung tâm chi phí thành công"));
    }

    @GetMapping("/all")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy toàn bộ trung tâm chi phí theo công ty (cho dropdown)")
    public ResponseEntity<ApiResponse<List<CostCenterResponse>>> getAllCostCenters(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        List<CostCenterResponse> result = costCenterService.getAllCostCentersByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách trung tâm chi phí thành công"));
    }

    @GetMapping("/{costCenterId}")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy thông tin chi tiết một trung tâm chi phí theo ID")
    public ResponseEntity<ApiResponse<CostCenterResponse>> getCostCenterById(
            @Parameter(description = "ID trung tâm chi phí", required = true)
            @PathVariable UUID costCenterId) {
        CostCenterResponse result = costCenterService.getCostCenterById(costCenterId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin trung tâm chi phí thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Tạo mới trung tâm chi phí")
    public ResponseEntity<ApiResponse<CostCenterResponse>> createCostCenter(@Valid @RequestBody CreateCostCenterRequest request) {
        CostCenterResponse result = costCenterService.createCostCenter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mới trung tâm chi phí thành công"));
    }

    @PutMapping("/{costCenterId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Cập nhật thông tin trung tâm chi phí")
    public ResponseEntity<ApiResponse<CostCenterResponse>> updateCostCenter(
            @Parameter(description = "ID trung tâm chi phí", required = true)
            @PathVariable UUID costCenterId,
            @Valid @RequestBody UpdateCostCenterRequest request) {
        CostCenterResponse result = costCenterService.updateCostCenter(costCenterId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật trung tâm chi phí thành công"));
    }

    @DeleteMapping("/{costCenterId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Xóa mềm trung tâm chi phí")
    public ResponseEntity<ApiResponse<Void>> deleteCostCenter(
            @Parameter(description = "ID trung tâm chi phí", required = true)
            @PathVariable UUID costCenterId) {
        costCenterService.deleteCostCenter(costCenterId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa trung tâm chi phí thành công"));
    }
}

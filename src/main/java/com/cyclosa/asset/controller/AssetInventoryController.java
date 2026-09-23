package com.cyclosa.asset.controller;

import com.cyclosa.asset.dto.request.CreateInventoryCheckRequest;
import com.cyclosa.asset.dto.response.InventoryCheckReportResponse;
import com.cyclosa.asset.dto.response.InventoryCheckResponse;
import com.cyclosa.asset.service.AssetInventoryService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
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
@RequestMapping("/api/v1/assets/inventory-checks")
@RequiredArgsConstructor
@Tag(name = "Asset Inventory Checks", description = "Kiểm kê định kỳ và báo cáo tài sản")
public class AssetInventoryController {

    private final AssetInventoryService assetInventoryService;

    @PostMapping
    @PreAuthorize("@perm.has('asset.inventory')")
    @RequirePermission("asset.inventory")
    @Operation(summary = "Tạo đợt kiểm kê tài sản định kỳ")
    public ResponseEntity<ApiResponse<InventoryCheckResponse>> createInventoryCheck(
            @Valid @RequestBody CreateInventoryCheckRequest request
    ) {
        InventoryCheckResponse response = assetInventoryService.createInventoryCheck(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Tạo đợt kiểm kê tài sản thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('asset.view')")
    @RequirePermission("asset.view")
    @Operation(summary = "Danh sách các đợt kiểm kê tài sản")
    public ResponseEntity<ApiResponse<PageData<InventoryCheckResponse>>> getInventoryChecks(
            @RequestParam(required = false) UUID companyId,
            @PageableDefault(sort = "checkDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageData<InventoryCheckResponse> response = assetInventoryService.getInventoryChecks(companyId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách đợt kiểm kê thành công"));
    }

    @GetMapping("/{id}/report")
    @PreAuthorize("@perm.has('asset.view')")
    @RequirePermission("asset.view")
    @Operation(summary = "Báo cáo chi tiết kết quả kiểm kê tài sản")
    public ResponseEntity<ApiResponse<InventoryCheckReportResponse>> getInventoryCheckReport(
            @PathVariable UUID id
    ) {
        InventoryCheckReportResponse response = assetInventoryService.getInventoryCheckReport(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy báo cáo kiểm kê tài sản thành công"));
    }
}

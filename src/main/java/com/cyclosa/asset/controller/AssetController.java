package com.cyclosa.asset.controller;

import com.cyclosa.asset.dto.request.*;
import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.asset.dto.response.AssetDetailResponse;
import com.cyclosa.asset.dto.response.AssetResponse;
import com.cyclosa.asset.service.AssetService;
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
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Asset Management", description = "Quản lý toàn diện tài sản, trang thiết bị, cấp phát và thu hồi")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("@perm.has('asset.create')")
    @RequirePermission("asset.create")
    @Operation(summary = "Đăng ký tài sản mới vào kho")
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(
            @Valid @RequestBody CreateAssetRequest request
    ) {
        AssetResponse response = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Đăng ký tài sản thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('asset.view')")
    @RequirePermission("asset.view")
    @Operation(summary = "Tra cứu danh sách tài sản có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<AssetResponse>>> getAssets(
            @Valid @ModelAttribute AssetFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageData<AssetResponse> response = assetService.getAssets(filter, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách tài sản thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('asset.view')")
    @RequirePermission("asset.view")
    @Operation(summary = "Xem thông tin chi tiết tài sản và lịch sử cấp phát")
    public ResponseEntity<ApiResponse<AssetDetailResponse>> getAssetById(
            @PathVariable UUID id
    ) {
        AssetDetailResponse response = assetService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin tài sản thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('asset.update')")
    @RequirePermission("asset.update")
    @Operation(summary = "Cập nhật thông tin tài sản")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssetRequest request
    ) {
        AssetResponse response = assetService.updateAsset(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật tài sản thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('asset.delete')")
    @RequirePermission("asset.delete")
    @Operation(summary = "Xóa mềm tài sản (chỉ cho phép khi tài sản không ở trạng thái ALLOCATED)")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @PathVariable UUID id
    ) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa tài sản thành công"));
    }

    @PostMapping("/{id}/allocate")
    @PreAuthorize("@perm.has('asset.allocate')")
    @RequirePermission("asset.allocate")
    @Operation(summary = "Cấp phát tài sản cho nhân viên")
    public ResponseEntity<ApiResponse<AssetAllocationResponse>> allocateAsset(
            @PathVariable UUID id,
            @Valid @RequestBody AllocateAssetRequest request
    ) {
        AssetAllocationResponse response = assetService.allocateAsset(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cấp phát tài sản cho nhân viên thành công"));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("@perm.has('asset.return')")
    @RequirePermission("asset.return")
    @Operation(summary = "Thu hồi tài sản từ nhân viên")
    public ResponseEntity<ApiResponse<AssetAllocationResponse>> returnAsset(
            @PathVariable UUID id,
            @Valid @RequestBody ReturnAssetRequest request
    ) {
        AssetAllocationResponse response = assetService.returnAsset(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Thu hồi tài sản thành công"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.has('asset.update')")
    @RequirePermission("asset.update")
    @Operation(summary = "Cập nhật trạng thái tài sản (bảo trì/sửa chữa/thanh lý)")
    public ResponseEntity<ApiResponse<AssetResponse>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeAssetStatusRequest request
    ) {
        AssetResponse response = assetService.changeStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái tài sản thành công"));
    }
}

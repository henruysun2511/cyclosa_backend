package com.cyclosa.asset.controller;

import com.cyclosa.asset.dto.request.CreateAssetAssignmentRequest;
import com.cyclosa.asset.dto.request.ReturnAssetRequest;
import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.asset.service.AssetService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asset-assignments")
@RequiredArgsConstructor
@Tag(name = "Asset Assignments", description = "Quản lý cấp phát và tiếp nhận hoàn trả bàn giao tài sản")
public class AssetAssignmentController {

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("@perm.has('asset.allocate') or @perm.has('asset.assign') or @perm.has('asset.manage')")
    @RequirePermission("asset.assign")
    @Operation(summary = "Cấp phát tài sản cho nhân viên sử dụng")
    public ResponseEntity<ApiResponse<AssetAllocationResponse>> createAssignment(
            @Valid @RequestBody CreateAssetAssignmentRequest request
    ) {
        AssetAllocationResponse response = assetService.allocateAsset(request.getAssetId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Cấp phát tài sản cho nhân viên thành công"));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("@perm.has('asset.return') or @perm.has('asset.assign') or @perm.has('asset.manage')")
    @RequirePermission("asset.assign")
    @Operation(summary = "Thu hồi / Tiếp nhận bàn giao tài sản hoàn trả")
    public ResponseEntity<ApiResponse<AssetAllocationResponse>> returnAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody ReturnAssetRequest request
    ) {
        AssetAllocationResponse response = assetService.returnAsset(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Thu hồi tài sản hoàn trả thành công"));
    }
}

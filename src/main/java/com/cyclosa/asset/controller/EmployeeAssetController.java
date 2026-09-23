package com.cyclosa.asset.controller;

import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.asset.service.AssetService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Asset Management", description = "Truy vấn danh sách tài sản được cấp phát của nhân viên")
public class EmployeeAssetController {

    private final AssetService assetService;

    @GetMapping("/{id}/assets")
    @PreAuthorize("@perm.has('asset.view')")
    @RequirePermission("asset.view")
    @Operation(summary = "Lấy danh sách tài sản nhân viên đang giữ và lịch sử cấp phát")
    public ResponseEntity<ApiResponse<PageData<AssetAllocationResponse>>> getEmployeeAssets(
            @PathVariable UUID id,
            @PageableDefault(sort = "allocatedDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageData<AssetAllocationResponse> response = assetService.getEmployeeAssets(id, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách tài sản của nhân viên thành công"));
    }
}

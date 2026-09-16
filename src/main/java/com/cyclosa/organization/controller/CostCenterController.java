package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.service.CostCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cost-centers")
@RequiredArgsConstructor
@Tag(name = "Cost Centers", description = "Quản lý trung tâm chi phí hạch toán")
public class CostCenterController {

    private final CostCenterService costCenterService;

    @GetMapping
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy danh sách các trung tâm chi phí của công ty")
    public ResponseEntity<ApiResponse<List<CostCenterResponse>>> getCostCenters(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                costCenterService.getCostCenters(effectiveCompanyId),
                "Lấy danh sách trung tâm chi phí thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('organization.manage')")
    @RequirePermission("organization.manage")
    @Operation(summary = "Tạo mới trung tâm chi phí")
    public ResponseEntity<ApiResponse<CostCenterResponse>> createCostCenter(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateCostCenterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        costCenterService.createCostCenter(headerCompanyId, request),
                        "Tạo trung tâm chi phí thành công"));
    }
}

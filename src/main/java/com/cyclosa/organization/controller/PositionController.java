package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.CreatePositionRequest;
import com.cyclosa.organization.dto.request.PositionFilter;
import com.cyclosa.organization.dto.request.UpdatePositionRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.dto.response.PositionDetailResponse;
import com.cyclosa.organization.dto.response.PositionResponse;
import com.cyclosa.organization.service.PositionService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Positions", description = "Quản lý cấp bậc và vị trí chức danh công việc")
public class PositionController {

    private final PositionService positionService;

    @GetMapping("/job-levels")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy danh sách các cấp bậc công việc theo thang thứ bậc")
    public ResponseEntity<ApiResponse<List<JobLevelResponse>>> getJobLevels(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                positionService.getJobLevels(effectiveCompanyId),
                "Lấy danh sách cấp bậc thành công"));
    }

    @PostMapping("/job-levels")
    @PreAuthorize("@perm.has('organization.manage')")
    @RequirePermission("organization.manage")
    @Operation(summary = "Tạo mới cấp bậc công việc")
    public ResponseEntity<ApiResponse<JobLevelResponse>> createJobLevel(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateJobLevelRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        positionService.createJobLevel(headerCompanyId, request),
                        "Tạo cấp bậc thành công"));
    }

    @GetMapping("/positions")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Tìm kiếm và phân trang danh sách vị trí chức danh")
    public ResponseEntity<ApiResponse<PageData<PositionResponse>>> getPositions(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @ModelAttribute PositionFilter filter
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                positionService.getPositions(effectiveCompanyId, filter),
                "Lấy danh sách chức danh thành công"));
    }

    @GetMapping("/positions/{id}")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy chi tiết thông tin chức danh việc làm")
    public ResponseEntity<ApiResponse<PositionDetailResponse>> getPositionById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                positionService.getPositionById(headerCompanyId, id),
                "Lấy thông tin chức danh thành công"));
    }

    @PostMapping("/positions")
    @PreAuthorize("@perm.has('organization.create')")
    @RequirePermission("organization.create")
    @Operation(summary = "Tạo mới vị trí chức danh")
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreatePositionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        positionService.createPosition(headerCompanyId, request),
                        "Tạo chức danh thành công"));
    }

    @PutMapping("/positions/{id}")
    @PreAuthorize("@perm.has('organization.update')")
    @RequirePermission("organization.update")
    @Operation(summary = "Cập nhật thông tin chức danh việc làm")
    public ResponseEntity<ApiResponse<PositionResponse>> updatePosition(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdatePositionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                positionService.updatePosition(headerCompanyId, id, request),
                "Cập nhật chức danh thành công"));
    }
}

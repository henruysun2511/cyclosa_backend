package com.cyclosa.performance.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.performance.dto.request.CreateKpiRequest;
import com.cyclosa.performance.dto.request.KpiFilter;
import com.cyclosa.performance.dto.request.UpdateKpiRequest;
import com.cyclosa.performance.dto.response.KpiResponse;
import com.cyclosa.performance.service.PerformanceService;
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
@RequestMapping("/api/v1/kpis")
@RequiredArgsConstructor
@Tag(name = "KPI Catalog", description = "Quản lý danh mục chỉ số hiệu suất KPI (toàn công ty hoặc theo phòng ban)")
public class KpiController {

    private final PerformanceService performanceService;

    @PostMapping
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Khai báo chỉ số KPI mới")
    public ResponseEntity<ApiResponse<KpiResponse>> createKpi(
            @Valid @RequestBody CreateKpiRequest request
    ) {
        KpiResponse response = performanceService.createKpi(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Tạo chỉ số KPI thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Cập nhật chỉ số KPI")
    public ResponseEntity<ApiResponse<KpiResponse>> updateKpi(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateKpiRequest request
    ) {
        KpiResponse response = performanceService.updateKpi(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật chỉ số KPI thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Lấy chi tiết KPI theo ID")
    public ResponseEntity<ApiResponse<KpiResponse>> getKpiById(@PathVariable UUID id) {
        KpiResponse response = performanceService.getKpiById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết KPI thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Tra cứu danh mục chỉ số KPI có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<KpiResponse>>> getKpis(
            @Valid @ModelAttribute KpiFilter filter
    ) {
        PageData<KpiResponse> response = performanceService.getKpis(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh mục KPI thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Xóa chỉ số KPI")
    public ResponseEntity<ApiResponse<Void>> deleteKpi(@PathVariable UUID id) {
        performanceService.deleteKpi(id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa chỉ số KPI thành công"));
    }
}

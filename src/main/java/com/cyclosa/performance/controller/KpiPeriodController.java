package com.cyclosa.performance.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.performance.dto.request.CreatePerformanceCycleRequest;
import com.cyclosa.performance.dto.request.PerformanceCycleFilter;
import com.cyclosa.performance.dto.request.UpdatePerformanceCycleRequest;
import com.cyclosa.performance.dto.response.PerformanceCycleResponse;
import com.cyclosa.performance.enums.PerformanceCycleStatus;
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
@RequestMapping({"/api/v1/kpi-periods", "/api/v1/performance-cycles"})
@RequiredArgsConstructor
@Tag(name = "Performance Cycle / KPI Periods", description = "Quản lý các chu kỳ đánh giá hiệu suất định kỳ (Tháng, Quý, Năm)")
public class KpiPeriodController {

    private final PerformanceService performanceService;

    @GetMapping
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Lấy danh sách các kỳ đánh giá hiệu suất có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<PerformanceCycleResponse>>> getCycles(
            @Valid @ModelAttribute PerformanceCycleFilter filter
    ) {
        PageData<PerformanceCycleResponse> response = performanceService.getCycles(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách kỳ đánh giá thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Khởi tạo kỳ đánh giá KPI mới")
    public ResponseEntity<ApiResponse<PerformanceCycleResponse>> createCycle(
            @Valid @RequestBody CreatePerformanceCycleRequest request
    ) {
        PerformanceCycleResponse response = performanceService.createCycle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Khởi tạo kỳ đánh giá KPI thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Lấy chi tiết kỳ đánh giá theo ID")
    public ResponseEntity<ApiResponse<PerformanceCycleResponse>> getCycleById(@PathVariable UUID id) {
        PerformanceCycleResponse response = performanceService.getCycleById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết kỳ đánh giá thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Cập nhật thông tin kỳ đánh giá")
    public ResponseEntity<ApiResponse<PerformanceCycleResponse>> updateCycle(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePerformanceCycleRequest request
    ) {
        PerformanceCycleResponse response = performanceService.updateCycle(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật kỳ đánh giá thành công"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Chuyển trạng thái kỳ đánh giá (DRAFT -> ACTIVE -> CLOSED)")
    public ResponseEntity<ApiResponse<PerformanceCycleResponse>> changeCycleStatus(
            @PathVariable UUID id,
            @RequestParam PerformanceCycleStatus status
    ) {
        PerformanceCycleResponse response = performanceService.changeCycleStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái kỳ đánh giá thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Xóa kỳ đánh giá (chỉ áp dụng khi ở trạng thái DRAFT)")
    public ResponseEntity<ApiResponse<Void>> deleteCycle(@PathVariable UUID id) {
        performanceService.deleteCycle(id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa kỳ đánh giá thành công"));
    }
}

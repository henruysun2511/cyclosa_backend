package com.cyclosa.performance.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.performance.dto.request.EvaluationFilter;
import com.cyclosa.performance.dto.request.FinalizeEvaluationRequest;
import com.cyclosa.performance.dto.request.SubmitManagerReviewRequest;
import com.cyclosa.performance.dto.request.SubmitSelfReviewRequest;
import com.cyclosa.performance.dto.response.PerformanceEvaluationDetailResponse;
import com.cyclosa.performance.dto.response.PerformanceEvaluationResponse;
import com.cyclosa.performance.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/kpi-evaluations", "/api/v1/performance/evaluations"})
@RequiredArgsConstructor
@Tag(name = "Performance Evaluations & Reviews", description = "Quản lý phiếu đánh giá hiệu suất, tự đánh giá cá nhân và quản lý chấm điểm")
public class KpiEvaluationController {

    private final PerformanceService performanceService;

    @GetMapping("/my")
    @PreAuthorize("@perm.has('performance.view_own')")
    @RequirePermission("performance.view_own")
    @Operation(summary = "Nhân viên xem phiếu đánh giá hiệu suất cá nhân (Self-review) trong kỳ hiện tại")
    public ResponseEntity<ApiResponse<PerformanceEvaluationDetailResponse>> getMyEvaluation(
            @RequestParam(required = false) UUID cycleId
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        PerformanceEvaluationDetailResponse response = performanceService.getMyEvaluation(currentUserId, cycleId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy phiếu đánh giá cá nhân thành công"));
    }

    @PutMapping("/{id}/self-review")
    @PreAuthorize("@perm.has('performance.view_own')")
    @RequirePermission("performance.view_own")
    @Operation(summary = "Nhân viên nộp bản tự đánh giá KPI và nhận xét cá nhân")
    public ResponseEntity<ApiResponse<PerformanceEvaluationDetailResponse>> submitSelfReview(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitSelfReviewRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        PerformanceEvaluationDetailResponse response = performanceService.submitSelfReview(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Nộp bản tự đánh giá hiệu suất thành công"));
    }

    @PutMapping("/{id}/manager-review")
    @PreAuthorize("@perm.has('performance.evaluate')")
    @RequirePermission("performance.evaluate")
    @Operation(summary = "Cấp quản lý trực tiếp chấm điểm và nhận xét xếp loại nhân viên")
    public ResponseEntity<ApiResponse<PerformanceEvaluationDetailResponse>> submitManagerReview(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitManagerReviewRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        PerformanceEvaluationDetailResponse response = performanceService.submitManagerReview(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Quản lý chấm điểm đánh giá thành công"));
    }

    @PutMapping("/{id}/finalize")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Chốt điểm đánh giá cuối kỳ và hoàn tất xếp loại (Finalize)")
    public ResponseEntity<ApiResponse<PerformanceEvaluationDetailResponse>> finalizeEvaluation(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) FinalizeEvaluationRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        PerformanceEvaluationDetailResponse response = performanceService.finalizeEvaluation(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Chốt điểm đánh giá cuối kỳ thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Tra cứu danh sách phiếu đánh giá hiệu suất theo kỳ/nhân viên có phân trang")
    public ResponseEntity<ApiResponse<PageData<PerformanceEvaluationResponse>>> getEvaluations(
            @Valid @ModelAttribute EvaluationFilter filter
    ) {
        PageData<PerformanceEvaluationResponse> response = performanceService.getEvaluations(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách phiếu đánh giá thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Xem chi tiết phiếu đánh giá kèm toàn bộ danh sách mục tiêu và điểm đánh giá")
    public ResponseEntity<ApiResponse<PerformanceEvaluationDetailResponse>> getEvaluationDetail(
            @PathVariable UUID id
    ) {
        PerformanceEvaluationDetailResponse response = performanceService.getEvaluationDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết phiếu đánh giá thành công"));
    }
}

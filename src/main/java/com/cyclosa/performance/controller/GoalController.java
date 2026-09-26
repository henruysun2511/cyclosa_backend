package com.cyclosa.performance.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.performance.dto.request.CreateGoalRequest;
import com.cyclosa.performance.dto.request.GoalFilter;
import com.cyclosa.performance.dto.request.UpdateGoalRequest;
import com.cyclosa.performance.dto.response.GoalResponse;
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
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Tag(name = "Performance Goals", description = "Quản lý mục tiêu hiệu suất của từng nhân viên theo từng kỳ")
public class GoalController {

    private final PerformanceService performanceService;

    @PostMapping
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Thiết lập mục tiêu cá nhân theo kỳ đánh giá (kiểm tra tổng trọng số ≤ 100%)")
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @Valid @RequestBody CreateGoalRequest request
    ) {
        GoalResponse response = performanceService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Tạo mục tiêu thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Cập nhật thông tin mục tiêu")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoalRequest request
    ) {
        GoalResponse response = performanceService.updateGoal(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật mục tiêu thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Lấy chi tiết mục tiêu theo ID")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(@PathVariable UUID id) {
        GoalResponse response = performanceService.getGoalById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết mục tiêu thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('performance.view')")
    @RequirePermission("performance.view")
    @Operation(summary = "Tra cứu danh sách mục tiêu có phân trang")
    public ResponseEntity<ApiResponse<PageData<GoalResponse>>> getGoals(
            @Valid @ModelAttribute GoalFilter filter
    ) {
        PageData<GoalResponse> response = performanceService.getGoals(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách mục tiêu thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('performance.manage')")
    @RequirePermission("performance.manage")
    @Operation(summary = "Xóa mục tiêu")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable UUID id) {
        performanceService.deleteGoal(id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa mục tiêu thành công"));
    }
}

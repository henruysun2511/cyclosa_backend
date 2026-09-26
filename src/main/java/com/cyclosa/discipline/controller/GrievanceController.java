package com.cyclosa.discipline.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.discipline.dto.request.CreateGrievanceRequest;
import com.cyclosa.discipline.dto.request.GrievanceFilter;
import com.cyclosa.discipline.dto.request.ResolveGrievanceRequest;
import com.cyclosa.discipline.dto.response.GrievanceDetailResponse;
import com.cyclosa.discipline.dto.response.GrievanceResponse;
import com.cyclosa.discipline.service.RewardDisciplineService;
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
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
@Tag(name = "Grievances", description = "Tiếp nhận và giải quyết khiếu nại, phản ánh nội bộ của nhân viên")
public class GrievanceController {

    private final RewardDisciplineService rewardDisciplineService;

    @PostMapping
    @PreAuthorize("@perm.has('grievance.apply')")
    @RequirePermission("grievance.apply")
    @Operation(summary = "Nhân viên gửi đơn khiếu nại, phản ánh nội bộ")
    public ResponseEntity<ApiResponse<GrievanceResponse>> createGrievance(
            @Valid @RequestBody CreateGrievanceRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        GrievanceResponse response = rewardDisciplineService.createGrievance(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Gửi đơn khiếu nại thành công"));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("@perm.has('grievance.manage')")
    @RequirePermission("grievance.manage")
    @Operation(summary = "HR / Ban giải quyết khiếu nại phản hồi và đóng đơn")
    public ResponseEntity<ApiResponse<GrievanceResponse>> resolveGrievance(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveGrievanceRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        GrievanceResponse response = rewardDisciplineService.resolveGrievance(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật giải quyết khiếu nại thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('grievance.view')")
    @RequirePermission("grievance.view")
    @Operation(summary = "Xem chi tiết đơn khiếu nại")
    public ResponseEntity<ApiResponse<GrievanceDetailResponse>> getGrievanceById(@PathVariable UUID id) {
        GrievanceDetailResponse response = rewardDisciplineService.getGrievanceById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin khiếu nại thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('grievance.view')")
    @RequirePermission("grievance.view")
    @Operation(summary = "Danh sách đơn khiếu nại có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<GrievanceResponse>>> getGrievances(
            @Valid @ModelAttribute GrievanceFilter filter
    ) {
        PageData<GrievanceResponse> response = rewardDisciplineService.getGrievances(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách khiếu nại thành công"));
    }
}

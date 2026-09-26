package com.cyclosa.discipline.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.discipline.dto.request.CreateRewardRequest;
import com.cyclosa.discipline.dto.request.RewardFilter;
import com.cyclosa.discipline.dto.request.UpdateRewardRequest;
import com.cyclosa.discipline.dto.response.RewardResponse;
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
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards", description = "Quản lý quyết định khen thưởng, tiền thưởng và vinh danh nhân viên")
public class RewardController {

    private final RewardDisciplineService rewardDisciplineService;

    @PostMapping
    @PreAuthorize("@perm.has('reward.manage')")
    @RequirePermission("reward.manage")
    @Operation(summary = "Khai báo quyết định khen thưởng cho nhân viên")
    public ResponseEntity<ApiResponse<RewardResponse>> createReward(
            @Valid @RequestBody CreateRewardRequest request
    ) {
        RewardResponse response = rewardDisciplineService.createReward(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Khai báo quyết định khen thưởng thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('reward.manage')")
    @RequirePermission("reward.manage")
    @Operation(summary = "Cập nhật quyết định khen thưởng (chưa đẩy vào bảng lương)")
    public ResponseEntity<ApiResponse<RewardResponse>> updateReward(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRewardRequest request
    ) {
        RewardResponse response = rewardDisciplineService.updateReward(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật quyết định khen thưởng thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('reward.view')")
    @RequirePermission("reward.view")
    @Operation(summary = "Lấy chi tiết quyết định khen thưởng")
    public ResponseEntity<ApiResponse<com.cyclosa.discipline.dto.response.RewardDetailResponse>> getRewardById(@PathVariable UUID id) {
        com.cyclosa.discipline.dto.response.RewardDetailResponse response = rewardDisciplineService.getRewardById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin khen thưởng thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('reward.view')")
    @RequirePermission("reward.view")
    @Operation(summary = "Danh sách quyết định khen thưởng có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<RewardResponse>>> getRewards(
            @Valid @ModelAttribute RewardFilter filter
    ) {
        PageData<RewardResponse> response = rewardDisciplineService.getRewards(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách khen thưởng thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('reward.manage')")
    @RequirePermission("reward.manage")
    @Operation(summary = "Xóa quyết định khen thưởng (chưa đẩy vào lương)")
    public ResponseEntity<ApiResponse<Void>> deleteReward(@PathVariable UUID id) {
        rewardDisciplineService.deleteReward(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa quyết định khen thưởng thành công"));
    }
}

package com.cyclosa.discipline.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.discipline.dto.request.CreateDisciplineRequest;
import com.cyclosa.discipline.dto.request.DisciplineFilter;
import com.cyclosa.discipline.dto.request.UpdateDisciplineRequest;
import com.cyclosa.discipline.dto.response.DisciplineResponse;
import com.cyclosa.discipline.dto.response.StatuteOfLimitationResponse;
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
@RequestMapping("/api/v1/disciplines")
@RequiredArgsConstructor
@Tag(name = "Disciplines", description = "Quản lý hồ sơ kỷ luật lao động, thời hiệu vi phạm và căn cứ pháp lý sa thải theo BLLĐ 2019")
public class DisciplineController {

    private final RewardDisciplineService rewardDisciplineService;

    @PostMapping
    @PreAuthorize("@perm.has('discipline.manage')")
    @RequirePermission("discipline.manage")
    @Operation(summary = "Lập hồ sơ xử lý kỷ luật lao động (tự động tính thời hiệu Điều 123 BLLĐ)")
    public ResponseEntity<ApiResponse<DisciplineResponse>> createDiscipline(
            @Valid @RequestBody CreateDisciplineRequest request
    ) {
        DisciplineResponse response = rewardDisciplineService.createDiscipline(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Lập hồ sơ kỷ luật thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('discipline.manage')")
    @RequirePermission("discipline.manage")
    @Operation(summary = "Cập nhật hồ sơ kỷ luật (khi chưa ban hành chính thức)")
    public ResponseEntity<ApiResponse<DisciplineResponse>> updateDiscipline(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDisciplineRequest request
    ) {
        DisciplineResponse response = rewardDisciplineService.updateDiscipline(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật hồ sơ kỷ luật thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('discipline.view')")
    @RequirePermission("discipline.view")
    @Operation(summary = "Xem chi tiết hồ sơ kỷ luật")
    public ResponseEntity<ApiResponse<com.cyclosa.discipline.dto.response.DisciplineDetailResponse>> getDisciplineById(@PathVariable UUID id) {
        com.cyclosa.discipline.dto.response.DisciplineDetailResponse response = rewardDisciplineService.getDisciplineById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin hồ sơ kỷ luật thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('discipline.view')")
    @RequirePermission("discipline.view")
    @Operation(summary = "Danh sách hồ sơ kỷ luật có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<DisciplineResponse>>> getDisciplines(
            @Valid @ModelAttribute DisciplineFilter filter
    ) {
        PageData<DisciplineResponse> response = rewardDisciplineService.getDisciplines(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách kỷ luật thành công"));
    }

    @GetMapping("/{id}/statute-of-limitation-check")
    @PreAuthorize("@perm.has('discipline.view')")
    @RequirePermission("discipline.view")
    @Operation(summary = "Kiểm tra thời hiệu xử lý kỷ luật lao động theo Điều 123 BLLĐ 2019")
    public ResponseEntity<ApiResponse<StatuteOfLimitationResponse>> checkStatuteOfLimitation(@PathVariable UUID id) {
        StatuteOfLimitationResponse response = rewardDisciplineService.checkStatuteOfLimitation(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Kiểm tra thời hiệu kỷ luật thành công"));
    }

    @PutMapping("/{id}/decide")
    @PreAuthorize("@perm.has('discipline.manage')")
    @RequirePermission("discipline.manage")
    @Operation(summary = "Ban hành quyết định kỷ luật chính thức (DECIDED, tính hạn xóa kỷ luật Điều 126)")
    public ResponseEntity<ApiResponse<DisciplineResponse>> decideDiscipline(@PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        DisciplineResponse response = rewardDisciplineService.decideDiscipline(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Ban hành quyết định kỷ luật thành công"));
    }

    @PutMapping("/{id}/approve-dismissal")
    @PreAuthorize("@perm.has('discipline.manage')")
    @RequirePermission("discipline.manage")
    @Operation(summary = "Phê duyệt thi hành kỷ luật sa thải nhân viên (SA_THAI) và kích hoạt thủ tục bàn giao thôi việc")
    public ResponseEntity<ApiResponse<DisciplineResponse>> approveDismissal(@PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        DisciplineResponse response = rewardDisciplineService.approveDismissal(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Phê duyệt quyết định sa thải và khởi tạo quy trình thôi việc thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('discipline.manage')")
    @RequirePermission("discipline.manage")
    @Operation(summary = "Xóa hồ sơ kỷ luật (chỉ áp dụng khi chưa DECIDED)")
    public ResponseEntity<ApiResponse<Void>> deleteDiscipline(@PathVariable UUID id) {
        rewardDisciplineService.deleteDiscipline(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa hồ sơ kỷ luật thành công"));
    }
}

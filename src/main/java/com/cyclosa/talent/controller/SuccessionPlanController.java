package com.cyclosa.talent.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.talent.dto.filter.SuccessionPlanFilter;
import com.cyclosa.talent.dto.request.AddSuccessionCandidateRequest;
import com.cyclosa.talent.dto.request.CreateSuccessionPlanRequest;
import com.cyclosa.talent.dto.request.UpdateSuccessionCandidateRequest;
import com.cyclosa.talent.dto.request.UpdateSuccessionPlanRequest;
import com.cyclosa.talent.dto.response.SuccessionCandidateResponse;
import com.cyclosa.talent.dto.response.SuccessionPlanDetailResponse;
import com.cyclosa.talent.dto.response.SuccessionPlanResponse;
import com.cyclosa.talent.service.SuccessionPlanService;
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
@RequestMapping("/api/v1/succession-plans")
@RequiredArgsConstructor
@Tag(name = "Succession Plans", description = "Quản lý kế hoạch kế nhiệm và danh sách ứng viên cho các vị trí trọng yếu")
public class SuccessionPlanController {

    private final SuccessionPlanService successionPlanService;

    @PostMapping
    @PreAuthorize("@perm.has('succession.manage')")
    @RequirePermission("succession.manage")
    @Operation(summary = "Lập kế hoạch kế nhiệm mới cho vị trí trọng yếu")
    public ResponseEntity<ApiResponse<SuccessionPlanResponse>> createSuccessionPlan(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateSuccessionPlanRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        SuccessionPlanResponse response = successionPlanService.createSuccessionPlan(effectiveCompanyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Lập kế hoạch kế nhiệm thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('succession.view')")
    @RequirePermission("succession.view")
    @Operation(summary = "Lấy danh sách các kế hoạch kế nhiệm (có lọc và phân trang)")
    public ResponseEntity<ApiResponse<PageData<SuccessionPlanResponse>>> getSuccessionPlans(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @ModelAttribute SuccessionPlanFilter filter
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                successionPlanService.getSuccessionPlans(effectiveCompanyId, filter),
                "Lấy danh sách kế hoạch kế nhiệm thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('succession.view')")
    @RequirePermission("succession.view")
    @Operation(summary = "Xem chi tiết kế hoạch kế nhiệm kèm danh sách ứng viên")
    public ResponseEntity<ApiResponse<SuccessionPlanDetailResponse>> getSuccessionPlanDetail(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                successionPlanService.getSuccessionPlanDetail(effectiveCompanyId, id),
                "Lấy chi tiết kế hoạch kế nhiệm thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('succession.manage')")
    @RequirePermission("succession.manage")
    @Operation(summary = "Cập nhật mức độ rủi ro hoặc ngày rà soát của kế hoạch kế nhiệm")
    public ResponseEntity<ApiResponse<SuccessionPlanResponse>> updateSuccessionPlan(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSuccessionPlanRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                successionPlanService.updateSuccessionPlan(effectiveCompanyId, id, request),
                "Cập nhật kế hoạch kế nhiệm thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('succession.manage')")
    @RequirePermission("succession.manage")
    @Operation(summary = "Xóa mềm kế hoạch kế nhiệm")
    public ResponseEntity<ApiResponse<Void>> deleteSuccessionPlan(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        successionPlanService.deleteSuccessionPlan(effectiveCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa kế hoạch kế nhiệm thành công"));
    }

    @PostMapping("/{id}/candidates")
    @PreAuthorize("@perm.has('succession.manage')")
    @RequirePermission("succession.manage")
    @Operation(summary = "Thêm ứng viên kế nhiệm vào vị trí (tự động kiểm tra không trùng với người đang giữ chức)")
    public ResponseEntity<ApiResponse<SuccessionCandidateResponse>> addCandidate(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody AddSuccessionCandidateRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        SuccessionCandidateResponse response = successionPlanService.addCandidate(effectiveCompanyId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Thêm ứng viên kế nhiệm thành công"));
    }

    @PutMapping("/{id}/candidates/{candidateId}")
    @PreAuthorize("@perm.has('succession.manage')")
    @RequirePermission("succession.manage")
    @Operation(summary = "Cập nhật mức độ sẵn sàng hoặc ghi chú của ứng viên kế nhiệm")
    public ResponseEntity<ApiResponse<SuccessionCandidateResponse>> updateCandidate(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @PathVariable UUID candidateId,
            @Valid @RequestBody UpdateSuccessionCandidateRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                successionPlanService.updateCandidate(effectiveCompanyId, id, candidateId, request),
                "Cập nhật ứng viên kế nhiệm thành công"));
    }

    @DeleteMapping("/{id}/candidates/{candidateId}")
    @PreAuthorize("@perm.has('succession.manage')")
    @RequirePermission("succession.manage")
    @Operation(summary = "Xóa ứng viên khỏi kế hoạch kế nhiệm")
    public ResponseEntity<ApiResponse<Void>> removeCandidate(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @PathVariable UUID candidateId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        successionPlanService.removeCandidate(effectiveCompanyId, id, candidateId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa ứng viên kế nhiệm thành công"));
    }
}

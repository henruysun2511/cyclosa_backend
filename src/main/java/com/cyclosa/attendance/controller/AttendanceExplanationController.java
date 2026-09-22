package com.cyclosa.attendance.controller;

import com.cyclosa.attendance.dto.request.CreateExplanationRequest;
import com.cyclosa.attendance.dto.request.ExplanationFilter;
import com.cyclosa.attendance.dto.response.AttendanceExplanationResponse;
import com.cyclosa.attendance.service.AttendanceExplanationService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance/explanations")
@RequiredArgsConstructor
@Tag(name = "Attendance Explanations", description = "Đơn giải trình chấm công (quên bấm thẻ, lỗi thiết bị, công tác) qua Workflow Engine")
public class AttendanceExplanationController {

    private final AttendanceExplanationService explanationService;

    @PostMapping
    @PreAuthorize("@perm.has('attendance.explain.apply')")
    @RequirePermission("attendance.explain.apply")
    @Operation(summary = "Gửi đơn giải trình chấm công (kích hoạt luồng duyệt Workflow Engine)")
    public ResponseEntity<ApiResponse<AttendanceExplanationResponse>> createExplanation(
            @Valid @RequestBody CreateExplanationRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        explanationService.createExplanation(currentUserId, request),
                        "Gửi đơn giải trình chấm công thành công"
                ));
    }

    @GetMapping("/my")
    @PreAuthorize("@perm.has('attendance.view_own')")
    @RequirePermission("attendance.view_own")
    @Operation(summary = "Xem danh sách đơn giải trình của bản thân")
    public ResponseEntity<ApiResponse<PageData<AttendanceExplanationResponse>>> getMyExplanations(
            @ModelAttribute ExplanationFilter filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                explanationService.getMyExplanations(currentUserId, filter, pageable),
                "Lấy danh sách đơn giải trình của bản thân thành công"
        ));
    }

    @GetMapping
    @PreAuthorize("@perm.has('attendance.explain.view')")
    @RequirePermission("attendance.explain.view")
    @Operation(summary = "Danh sách đơn giải trình toàn đơn vị theo DataScope")
    public ResponseEntity<ApiResponse<PageData<AttendanceExplanationResponse>>> getExplanations(
            @ModelAttribute ExplanationFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                explanationService.getExplanations(headerCompanyId, filter, pageable),
                "Lấy danh sách đơn giải trình thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('attendance.explain.view')")
    @RequirePermission("attendance.explain.view")
    @Operation(summary = "Xem chi tiết đơn giải trình và trạng thái phê duyệt")
    public ResponseEntity<ApiResponse<com.cyclosa.attendance.dto.response.AttendanceExplanationDetailResponse>> getExplanationById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                explanationService.getExplanationById(headerCompanyId, id),
                "Lấy thông tin đơn giải trình thành công"
        ));
    }
}

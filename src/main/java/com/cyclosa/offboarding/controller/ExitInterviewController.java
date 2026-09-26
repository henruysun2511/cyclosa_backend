package com.cyclosa.offboarding.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.offboarding.dto.request.SubmitExitInterviewRequest;
import com.cyclosa.offboarding.dto.response.ExitInterviewDetailResponse;
import com.cyclosa.offboarding.dto.response.ExitInterviewResponse;
import com.cyclosa.offboarding.service.OffboardingService;
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
@RequestMapping("/api/v1/offboarding/exit-interviews")
@RequiredArgsConstructor
@Tag(name = "Offboarding - Exit Interviews", description = "Quản lý phỏng vấn thôi việc, ghi nhận phản hồi và lý do nghỉ việc")
public class ExitInterviewController {

    private final OffboardingService offboardingService;

    @PostMapping
    @PreAuthorize("@perm.has('offboarding.manage')")
    @RequirePermission("offboarding.manage")
    @Operation(summary = "Ghi nhận kết quả phỏng vấn thôi việc (Exit Interview)")
    public ResponseEntity<ApiResponse<ExitInterviewResponse>> submitExitInterview(
            @Valid @RequestBody SubmitExitInterviewRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ExitInterviewResponse response = offboardingService.submitExitInterview(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Ghi nhận phỏng vấn thôi việc thành công"));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@perm.has('offboarding.view')")
    @RequirePermission("offboarding.view")
    @Operation(summary = "Xem thông tin phỏng vấn thôi việc của nhân viên")
    public ResponseEntity<ApiResponse<ExitInterviewDetailResponse>> getExitInterview(
            @PathVariable UUID employeeId
    ) {
        ExitInterviewDetailResponse response = offboardingService.getExitInterview(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin phỏng vấn thành công"));
    }
}

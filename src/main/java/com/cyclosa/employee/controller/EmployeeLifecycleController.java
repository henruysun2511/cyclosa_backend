package com.cyclosa.employee.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.employee.dto.request.ChangeStatusRequest;
import com.cyclosa.employee.dto.request.CreateProbationEvaluationRequest;
import com.cyclosa.employee.dto.response.OfficialEligibilityResponse;
import com.cyclosa.employee.dto.response.ProbationEvaluationResponse;
import com.cyclosa.employee.dto.response.StatusHistoryResponse;
import com.cyclosa.employee.service.EmployeeLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Lifecycle & Probation", description = "Quản lý vòng đời nhân sự, biến động trạng thái, thử việc & chuyển chính thức")
public class EmployeeLifecycleController {

    private final EmployeeLifecycleService lifecycleService;

    @GetMapping("/{employeeId}/status-history")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Lịch sử biến động trạng thái làm việc của nhân viên")
    public ResponseEntity<ApiResponse<List<StatusHistoryResponse>>> getStatusHistory(
            @PathVariable UUID employeeId) {
        List<StatusHistoryResponse> result = lifecycleService.getStatusHistory(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy lịch sử trạng thái thành công"));
    }

    @PostMapping("/{employeeId}/status-changes")
    @PreAuthorize("@perm.has('employee.status')")
    @RequirePermission("employee.status")
    @Operation(summary = "Chuyển trạng thái làm việc (Tạm đình chỉ, Nghỉ thai sản, Thôi việc...)")
    public ResponseEntity<ApiResponse<StatusHistoryResponse>> changeStatus(
            @PathVariable UUID employeeId,
            @Valid @RequestBody ChangeStatusRequest request) {
        StatusHistoryResponse result = lifecycleService.changeStatus(employeeId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Chuyển trạng thái làm việc thành công"));
    }

    @GetMapping("/{employeeId}/probation-evaluations")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Xem lịch sử kết quả đánh giá thử việc")
    public ResponseEntity<ApiResponse<List<ProbationEvaluationResponse>>> getProbationEvaluations(
            @PathVariable UUID employeeId) {
        List<ProbationEvaluationResponse> result = lifecycleService.getProbationEvaluations(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy kết quả đánh giá thử việc thành công"));
    }

    @PostMapping("/{employeeId}/probation-evaluations")
    @PreAuthorize("@perm.has('employee.status')")
    @RequirePermission("employee.status")
    @Operation(summary = "Ghi nhận kết quả đánh giá thử việc (PASSED / FAILED / EXTENDED)")
    public ResponseEntity<ApiResponse<ProbationEvaluationResponse>> createProbationEvaluation(
            @PathVariable UUID employeeId,
            @Valid @RequestBody CreateProbationEvaluationRequest request) {
        ProbationEvaluationResponse result = lifecycleService.createProbationEvaluation(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Ghi nhận đánh giá thử việc thành công"));
    }

    @GetMapping("/{employeeId}/official-eligibility")
    @PreAuthorize("@perm.has('employee.status')")
    @RequirePermission("employee.status")
    @Operation(summary = "Thẩm định tự động các điều kiện để chuyển sang nhân viên chính thức")
    public ResponseEntity<ApiResponse<OfficialEligibilityResponse>> checkOfficialEligibility(
            @PathVariable UUID employeeId) {
        OfficialEligibilityResponse result = lifecycleService.checkOfficialEligibility(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Thẩm định điều kiện chuyển chính thức thành công"));
    }

    @PostMapping("/{employeeId}/confirm-official")
    @PreAuthorize("@perm.has('employee.status')")
    @RequirePermission("employee.status")
    @Operation(summary = "Phê duyệt chuyển nhân viên thử việc sang nhân viên chính thức (ACTIVE)")
    public ResponseEntity<ApiResponse<StatusHistoryResponse>> confirmOfficial(
            @PathVariable UUID employeeId) {
        StatusHistoryResponse result = lifecycleService.confirmOfficial(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Xác nhận chuyển chính thức thành công"));
    }
}


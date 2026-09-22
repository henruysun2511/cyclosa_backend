package com.cyclosa.leave.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.leave.dto.request.CreateLeaveRequestRequest;
import com.cyclosa.leave.dto.request.LeaveRequestFilter;
import com.cyclosa.leave.dto.request.RejectLeaveRequest;
import com.cyclosa.leave.dto.response.LeaveRequestDetailResponse;
import com.cyclosa.leave.dto.response.LeaveRequestResponse;
import com.cyclosa.leave.service.LeaveRequestService;
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
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
@Tag(name = "Leave Requests", description = "Quản lý đơn xin nghỉ phép và quy trình phê duyệt đa cấp")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    @PreAuthorize("@perm.has('leave.apply')")
    @RequirePermission("leave.apply")
    @Operation(summary = "Tạo mới đơn xin nghỉ phép (kèm tự động kiểm tra số dư và kích hoạt Workflow)")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> createLeaveRequest(
            @Valid @RequestBody CreateLeaveRequestRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        leaveRequestService.createLeaveRequest(currentUserId, request),
                        "Gửi đơn xin nghỉ phép thành công"
                ));
    }

    @GetMapping("/my")
    @Operation(summary = "Xem lịch sử đơn xin nghỉ phép của bản thân (ESS)")
    public ResponseEntity<ApiResponse<PageData<LeaveRequestResponse>>> getMyLeaveRequests(
            @ModelAttribute LeaveRequestFilter filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveRequestService.getMyLeaveRequests(filter, pageable),
                "Lấy danh sách đơn nghỉ phép cá nhân thành công"
        ));
    }

    @GetMapping
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Danh sách đơn xin nghỉ phép theo phân quyền dữ liệu (DataScope)")
    public ResponseEntity<ApiResponse<PageData<LeaveRequestResponse>>> getLeaveRequests(
            @ModelAttribute LeaveRequestFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveRequestService.getLeaveRequests(headerCompanyId, filter, pageable),
                "Lấy danh sách đơn nghỉ phép thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Xem chi tiết một đơn xin nghỉ phép kèm lịch sử luồng duyệt Workflow")
    public ResponseEntity<ApiResponse<LeaveRequestDetailResponse>> getLeaveRequestById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveRequestService.getLeaveRequestById(headerCompanyId, id),
                "Lấy chi tiết đơn nghỉ phép thành công"
        ));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("@perm.has('leave.approve')")
    @RequirePermission("leave.approve")
    @Operation(summary = "Phê duyệt đơn xin nghỉ phép")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeaveRequest(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveRequestService.approveLeaveRequest(headerCompanyId, id),
                "Phê duyệt đơn nghỉ phép thành công"
        ));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("@perm.has('leave.approve')")
    @RequirePermission("leave.approve")
    @Operation(summary = "Từ chối đơn xin nghỉ phép kèm lý do")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectLeaveRequest(
            @PathVariable UUID id,
            @Valid @RequestBody RejectLeaveRequest rejectReq,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveRequestService.rejectLeaveRequest(headerCompanyId, id, rejectReq),
                "Từ chối đơn nghỉ phép thành công"
        ));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("@perm.has('leave.apply')")
    @RequirePermission("leave.apply")
    @Operation(summary = "Hủy đơn xin nghỉ phép (chỉ khi ngày nghỉ chưa bắt đầu)")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> cancelLeaveRequest(@PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                leaveRequestService.cancelLeaveRequest(currentUserId, id),
                "Hủy đơn nghỉ phép thành công"
        ));
    }
}

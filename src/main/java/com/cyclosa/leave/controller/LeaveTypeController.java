package com.cyclosa.leave.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.leave.dto.request.CreateLeaveTypeRequest;
import com.cyclosa.leave.dto.request.LeaveTypeFilter;
import com.cyclosa.leave.dto.request.UpdateLeaveTypeRequest;
import com.cyclosa.leave.dto.response.LeaveTypeResponse;
import com.cyclosa.leave.service.LeaveTypeService;
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
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
@Tag(name = "Leave Types", description = "Quản lý danh mục các loại ngày nghỉ phép theo quy định pháp luật")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Lấy danh sách các loại ngày nghỉ (phân trang và tìm kiếm)")
    public ResponseEntity<ApiResponse<PageData<LeaveTypeResponse>>> getLeaveTypes(
            @ModelAttribute LeaveTypeFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveTypeService.getLeaveTypes(headerCompanyId, filter, pageable),
                "Lấy danh sách loại ngày nghỉ thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Lấy chi tiết một loại ngày nghỉ")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> getLeaveTypeById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveTypeService.getLeaveTypeById(headerCompanyId, id),
                "Lấy thông tin loại ngày nghỉ thành công"
        ));
    }

    @PostMapping
    @PreAuthorize("@perm.has('leave.manage')")
    @RequirePermission("leave.manage")
    @Operation(summary = "Tạo mới loại ngày nghỉ phép")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> createLeaveType(
            @Valid @RequestBody CreateLeaveTypeRequest request,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        leaveTypeService.createLeaveType(headerCompanyId, request),
                        "Tạo loại ngày nghỉ thành công"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('leave.manage')")
    @RequirePermission("leave.manage")
    @Operation(summary = "Cập nhật thông tin loại ngày nghỉ phép")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> updateLeaveType(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLeaveTypeRequest request,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveTypeService.updateLeaveType(headerCompanyId, id, request),
                "Cập nhật loại ngày nghỉ thành công"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('leave.manage')")
    @RequirePermission("leave.manage")
    @Operation(summary = "Xóa mềm loại ngày nghỉ phép")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveType(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        leaveTypeService.deleteLeaveType(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa loại ngày nghỉ thành công"));
    }
}

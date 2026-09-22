package com.cyclosa.attendance.controller;

import com.cyclosa.attendance.dto.request.BatchShiftAssignmentRequest;
import com.cyclosa.attendance.dto.request.ShiftAssignmentFilter;
import com.cyclosa.attendance.dto.response.ShiftAssignmentResponse;
import com.cyclosa.attendance.service.ShiftAssignmentService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shift-assignments")
@RequiredArgsConstructor
@Tag(name = "Shift Assignments", description = "Phân ca làm việc và quản lý lịch trình làm việc (Roster / Scheduling)")
public class ShiftAssignmentController {

    private final ShiftAssignmentService assignmentService;

    @PostMapping("/batch")
    @PreAuthorize("@perm.has('attendance.schedule.manage')")
    @RequirePermission("attendance.schedule.manage")
    @Operation(summary = "Phân ca hàng loạt theo nhân viên/phòng ban/tuần")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentResponse>>> batchAssignShifts(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody BatchShiftAssignmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        assignmentService.batchAssignShifts(headerCompanyId, request),
                        "Phân ca làm việc hàng loạt thành công"
                ));
    }

    @GetMapping("/my-schedule")
    @PreAuthorize("@perm.has('attendance.view_own')")
    @RequirePermission("attendance.view_own")
    @Operation(summary = "Xem lịch làm việc cá nhân trong tuần/tháng")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentResponse>>> getMySchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                assignmentService.getMySchedule(fromDate, toDate),
                "Lấy lịch làm việc cá nhân thành công"
        ));
    }

    @GetMapping
    @PreAuthorize("@perm.has('attendance.schedule.view')")
    @RequirePermission("attendance.schedule.view")
    @Operation(summary = "Tra cứu lịch phân ca toàn công ty/phòng ban theo DataScope")
    public ResponseEntity<ApiResponse<PageData<ShiftAssignmentResponse>>> getShiftAssignments(
            @ModelAttribute ShiftAssignmentFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                assignmentService.getShiftAssignments(headerCompanyId, filter, pageable),
                "Lấy danh sách phân ca thành công"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('attendance.schedule.manage')")
    @RequirePermission("attendance.schedule.manage")
    @Operation(summary = "Hủy phân ca làm việc")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        assignmentService.deleteAssignment(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Hủy phân ca thành công"));
    }
}

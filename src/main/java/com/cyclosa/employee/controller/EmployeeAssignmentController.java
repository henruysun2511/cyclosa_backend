package com.cyclosa.employee.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.employee.dto.request.CreateWorkAssignmentRequest;
import com.cyclosa.employee.dto.response.WorkAssignmentResponse;
import com.cyclosa.employee.service.EmployeeAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Assignments", description = "Quản lý phân công công tác 3 chiều theo thời gian và quản lý cấp dưới")
public class EmployeeAssignmentController {

    private final EmployeeAssignmentService assignmentService;

    @GetMapping("/{employeeId}/work-assignments")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Lấy toàn bộ lịch sử công tác 3 chiều của nhân viên")
    public ResponseEntity<ApiResponse<List<WorkAssignmentResponse>>> getWorkAssignments(
            @PathVariable UUID employeeId) {
        List<WorkAssignmentResponse> result = assignmentService.getWorkAssignments(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy lịch sử công tác thành công"));
    }

    @GetMapping("/{employeeId}/work-assignments/at")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Lấy phân công công tác có hiệu lực tại ngày chỉ định")
    public ResponseEntity<ApiResponse<WorkAssignmentResponse>> getWorkAssignmentAt(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        WorkAssignmentResponse result = assignmentService.getWorkAssignmentAt(employeeId, date);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy công tác tại thời điểm thành công"));
    }

    @PostMapping("/{employeeId}/work-assignments")
    @PreAuthorize("@perm.has('employee.assign')")
    @RequirePermission("employee.assign")
    @Operation(summary = "Phân công công tác mới (Bổ nhiệm, luân chuyển phòng ban, chi nhánh, chức danh)")
    public ResponseEntity<ApiResponse<WorkAssignmentResponse>> createWorkAssignment(
            @PathVariable UUID employeeId,
            @Valid @RequestBody CreateWorkAssignmentRequest request) {
        WorkAssignmentResponse result = assignmentService.createWorkAssignment(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Phân công công tác mới thành công"));
    }

    @GetMapping("/{employeeId}/direct-reports")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Danh sách nhân viên cấp dưới trực tiếp do nhân sự này quản lý")
    public ResponseEntity<ApiResponse<List<WorkAssignmentResponse>>> getDirectReports(
            @PathVariable UUID employeeId) {
        List<WorkAssignmentResponse> result = assignmentService.getDirectReports(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách cấp dưới trực tiếp thành công"));
    }
}


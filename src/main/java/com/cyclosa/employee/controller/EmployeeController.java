package com.cyclosa.employee.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.request.CreateEmployeeRequest;
import com.cyclosa.employee.dto.request.EmployeeFilterRequest;
import com.cyclosa.employee.dto.request.UpdateEmployeeRequest;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.dto.response.EmployeeSummaryResponse;
import com.cyclosa.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Employee Profile", description = "Quản lý hồ sơ nhân sự cốt lõi và thông tin cá nhân")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Lấy danh sách nhân viên phân trang", description = "Hỗ trợ lọc theo phòng ban, chi nhánh, chức danh, trạng thái và từ khóa")
    public ResponseEntity<ApiResponse<PageData<EmployeeSummaryResponse>>> getEmployees(
            @ParameterObject @Valid EmployeeFilterRequest filter) {
        PageData<EmployeeSummaryResponse> result = employeeService.getEmployees(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách nhân viên thành công"));
    }

    @PostMapping("/employees")
    @PreAuthorize("@perm.has('employee.create')")
    @RequirePermission("employee.create")
    @Operation(summary = "Tiếp nhận nhân sự mới", description = "Khởi tạo hồ sơ, cấp mã NV tự động, gán bộ 3 chiều công tác ban đầu và tùy chọn cấp tài khoản")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeDetailResponse result = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tiếp nhận nhân sự mới thành công"));
    }

    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Xem hồ sơ đầy đủ chi tiết của nhân viên")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getEmployeeById(
            @Parameter(description = "ID nhân viên", required = true)
            @PathVariable UUID employeeId) {
        EmployeeDetailResponse result = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin nhân viên thành công"));
    }

    @PutMapping("/employees/{employeeId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Cập nhật thông tin lý lịch cá nhân và pháp lý")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> updateEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeDetailResponse result = employeeService.updateEmployee(employeeId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật thông tin nhân viên thành công"));
    }

    @GetMapping("/me/profile")
    @Operation(summary = "Xem hồ sơ của tôi (My Profile)")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getMyProfile() {
        EmployeeDetailResponse result = employeeService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin hồ sơ của tôi thành công"));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Nhân viên tự cập nhật thông tin cá nhân cơ bản")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> updateMyProfile(
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeDetailResponse result = employeeService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật hồ sơ cá nhân thành công"));
    }
}


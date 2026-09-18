package com.cyclosa.employee.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.request.*;
import com.cyclosa.employee.dto.response.*;
import com.cyclosa.employee.service.EmployeeService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Quản lý hồ sơ nhân sự toàn diện và vòng đời nhân viên")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Tìm kiếm và phân trang danh sách nhân viên (tự động bảo vệ theo DataScope)")
    public ResponseEntity<ApiResponse<PageData<EmployeeResponse>>> getEmployees(
            @ModelAttribute EmployeeFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getEmployees(headerCompanyId, filter, pageable),
                "Lấy danh sách nhân sự thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Xem chi tiết hồ sơ toàn diện của nhân sự theo ID")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getEmployeeById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getEmployeeById(headerCompanyId, id),
                "Lấy thông tin nhân viên thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('employee.create')")
    @RequirePermission("employee.create")
    @Operation(summary = "Tiếp nhận nhân sự mới (hồ sơ 3 chiều + tùy chọn tạo tài khoản User)")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> createEmployee(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        employeeService.createEmployee(headerCompanyId, request),
                        "Tiếp nhận nhân viên mới thành công"));
    }

    @PutMapping("/{id}/personal-info")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Cập nhật thông tin cá nhân, CCCD, mã số thuế, tài khoản ngân hàng")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> updatePersonalInfo(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdatePersonalInfoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.updatePersonalInfo(headerCompanyId, id, request),
                "Cập nhật thông tin cá nhân thành công"));
    }

    @PutMapping("/{id}/employment-info")
    @PreAuthorize("@perm.has('employee.manage_job')")
    @RequirePermission("employee.manage_job")
    @Operation(summary = "Điều chuyển công tác, bổ nhiệm chức danh, thay đổi quản lý trực tiếp")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> updateEmploymentInfo(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdateEmploymentInfoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.updateEmploymentInfo(headerCompanyId, id, request),
                "Cập nhật thông tin công tác thành công"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.has('employee.manage_status')")
    @RequirePermission("employee.manage_status")
    @Operation(summary = "Thay đổi trạng thái vòng đời nhân sự (tự động khóa User khi nghỉ việc)")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> changeStatus(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody ChangeEmployeeStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.changeStatus(headerCompanyId, id, request),
                "Thay đổi trạng thái nhân sự thành công"));
    }

    @GetMapping("/{id}/dependents")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Danh sách người phụ thuộc giảm trừ gia cảnh thuế TNCN")
    public ResponseEntity<ApiResponse<List<EmployeeDependentResponse>>> getDependents(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getDependents(headerCompanyId, id),
                "Lấy danh sách người phụ thuộc thành công"));
    }

    @PostMapping("/{id}/dependents")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Đăng ký người phụ thuộc mới cho nhân sự")
    public ResponseEntity<ApiResponse<EmployeeDependentResponse>> createDependent(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateDependentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        employeeService.createDependent(headerCompanyId, id, request),
                        "Thêm người phụ thuộc thành công"));
    }

    @DeleteMapping("/{id}/dependents/{depId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Xóa thông tin người phụ thuộc")
    public ResponseEntity<ApiResponse<Void>> deleteDependent(
            @PathVariable UUID id,
            @PathVariable UUID depId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        employeeService.deleteDependent(headerCompanyId, id, depId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa người phụ thuộc thành công"));
    }

    @GetMapping("/{id}/emergency-contacts")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Danh sách người liên hệ khẩn cấp của nhân sự")
    public ResponseEntity<ApiResponse<List<EmployeeEmergencyContactResponse>>> getEmergencyContacts(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getEmergencyContacts(headerCompanyId, id),
                "Lấy danh sách liên hệ khẩn cấp thành công"));
    }

    @PostMapping("/{id}/emergency-contacts")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Thêm người liên hệ khẩn cấp")
    public ResponseEntity<ApiResponse<EmployeeEmergencyContactResponse>> createEmergencyContact(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateEmergencyContactRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        employeeService.createEmergencyContact(headerCompanyId, id, request),
                        "Thêm người liên hệ khẩn cấp thành công"));
    }

    @DeleteMapping("/{id}/emergency-contacts/{contactId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Xóa người liên hệ khẩn cấp")
    public ResponseEntity<ApiResponse<Void>> deleteEmergencyContact(
            @PathVariable UUID id,
            @PathVariable UUID contactId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        employeeService.deleteEmergencyContact(headerCompanyId, id, contactId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa người liên hệ khẩn cấp thành công"));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Lịch sử biến động công tác của nhân sự (Audit Trail)")
    public ResponseEntity<ApiResponse<List<EmployeeHistoryResponse>>> getEmployeeHistory(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getEmployeeHistory(headerCompanyId, id),
                "Lấy lịch sử biến động công tác thành công"));
    }
}

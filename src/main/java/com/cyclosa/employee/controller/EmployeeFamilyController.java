package com.cyclosa.employee.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.employee.dto.request.CreateDependentRequest;
import com.cyclosa.employee.dto.request.CreateEmergencyContactRequest;
import com.cyclosa.employee.dto.response.DependentResponse;
import com.cyclosa.employee.dto.response.EmergencyContactResponse;
import com.cyclosa.employee.service.EmployeeFamilyService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Employee Dependents & Emergency", description = "Quản lý người phụ thuộc giảm trừ gia cảnh và liên hệ khẩn cấp")
public class EmployeeFamilyController {

    private final EmployeeFamilyService familyService;

    @GetMapping("/employees/{employeeId}/dependents")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Danh sách người phụ thuộc đăng ký giảm trừ gia cảnh thuế")
    public ResponseEntity<ApiResponse<List<DependentResponse>>> getDependents(@PathVariable UUID employeeId) {
        List<DependentResponse> result = familyService.getDependents(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách người phụ thuộc thành công"));
    }

    @PostMapping("/employees/{employeeId}/dependents")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Đăng ký thêm người phụ thuộc mới")
    public ResponseEntity<ApiResponse<DependentResponse>> createDependent(
            @PathVariable UUID employeeId,
            @Valid @RequestBody CreateDependentRequest request) {
        DependentResponse result = familyService.createDependent(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Thêm người phụ thuộc thành công"));
    }

    @PutMapping("/dependents/{dependentId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Cập nhật thông tin người phụ thuộc & thời gian giảm trừ thuế")
    public ResponseEntity<ApiResponse<DependentResponse>> updateDependent(
            @PathVariable UUID dependentId,
            @Valid @RequestBody CreateDependentRequest request) {
        DependentResponse result = familyService.updateDependent(dependentId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật người phụ thuộc thành công"));
    }

    @DeleteMapping("/dependents/{dependentId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Xóa người phụ thuộc")
    public ResponseEntity<ApiResponse<Void>> deleteDependent(@PathVariable UUID dependentId) {
        familyService.deleteDependent(dependentId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa người phụ thuộc thành công"));
    }

    @GetMapping("/employees/{employeeId}/emergency-contacts")
    @PreAuthorize("@perm.has('employee.view')")
    @RequirePermission("employee.view")
    @Operation(summary = "Danh sách người liên hệ khẩn cấp")
    public ResponseEntity<ApiResponse<List<EmergencyContactResponse>>> getEmergencyContacts(@PathVariable UUID employeeId) {
        List<EmergencyContactResponse> result = familyService.getEmergencyContacts(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách người liên hệ khẩn cấp thành công"));
    }

    @PostMapping("/employees/{employeeId}/emergency-contacts")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Thêm người liên hệ khẩn cấp")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> createEmergencyContact(
            @PathVariable UUID employeeId,
            @Valid @RequestBody CreateEmergencyContactRequest request) {
        EmergencyContactResponse result = familyService.createEmergencyContact(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Thêm người liên hệ khẩn cấp thành công"));
    }

    @PutMapping("/emergency-contacts/{contactId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Cập nhật người liên hệ khẩn cấp")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> updateEmergencyContact(
            @PathVariable UUID contactId,
            @Valid @RequestBody CreateEmergencyContactRequest request) {
        EmergencyContactResponse result = familyService.updateEmergencyContact(contactId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật người liên hệ khẩn cấp thành công"));
    }

    @DeleteMapping("/emergency-contacts/{contactId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Xóa người liên hệ khẩn cấp")
    public ResponseEntity<ApiResponse<Void>> deleteEmergencyContact(@PathVariable UUID contactId) {
        familyService.deleteEmergencyContact(contactId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa người liên hệ khẩn cấp thành công"));
    }
}


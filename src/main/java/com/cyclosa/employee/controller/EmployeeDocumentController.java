package com.cyclosa.employee.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.employee.dto.request.CreateIdentityDocumentRequest;
import com.cyclosa.employee.dto.request.UpdateBankAccountRequest;
import com.cyclosa.employee.dto.request.UpdateIdentityDocumentRequest;
import com.cyclosa.employee.dto.response.BankAccountResponse;
import com.cyclosa.employee.dto.response.IdentityDocumentResponse;
import com.cyclosa.employee.service.EmployeeDocumentService;
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
@Tag(name = "Employee Documents & Banking", description = "Quản lý giấy tờ định danh và tài khoản ngân hàng nhận lương")
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;

    @GetMapping("/employees/{employeeId}/identity-documents")
    @PreAuthorize("@perm.has('employee.view_sensitive')")
    @RequirePermission("employee.view_sensitive")
    @Operation(summary = "Danh sách giấy tờ định danh (CCCD, Hộ chiếu) của nhân viên")
    public ResponseEntity<ApiResponse<List<IdentityDocumentResponse>>> getIdentityDocuments(
            @PathVariable UUID employeeId) {
        List<IdentityDocumentResponse> result = documentService.getIdentityDocuments(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách giấy tờ định danh thành công"));
    }

    @PostMapping("/employees/{employeeId}/identity-documents")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Thêm giấy tờ định danh mới cho nhân viên")
    public ResponseEntity<ApiResponse<IdentityDocumentResponse>> createIdentityDocument(
            @PathVariable UUID employeeId,
            @Valid @RequestBody CreateIdentityDocumentRequest request) {
        IdentityDocumentResponse result = documentService.createIdentityDocument(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Thêm giấy tờ định danh thành công"));
    }

    @PutMapping("/identity-documents/{documentId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Cập nhật giấy tờ định danh")
    public ResponseEntity<ApiResponse<IdentityDocumentResponse>> updateIdentityDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody UpdateIdentityDocumentRequest request) {
        IdentityDocumentResponse result = documentService.updateIdentityDocument(documentId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật giấy tờ định danh thành công"));
    }

    @DeleteMapping("/identity-documents/{documentId}")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Xóa giấy tờ định danh")
    public ResponseEntity<ApiResponse<Void>> deleteIdentityDocument(@PathVariable UUID documentId) {
        documentService.deleteIdentityDocument(documentId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa giấy tờ định danh thành công"));
    }

    @GetMapping("/employees/{employeeId}/bank-account")
    @PreAuthorize("@perm.has('employee.view_sensitive')")
    @RequirePermission("employee.view_sensitive")
    @Operation(summary = "Xem tài khoản ngân hàng nhận lương của nhân viên")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccount(@PathVariable UUID employeeId) {
        BankAccountResponse result = documentService.getBankAccount(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin tài khoản ngân hàng thành công"));
    }

    @PutMapping("/employees/{employeeId}/bank-account")
    @PreAuthorize("@perm.has('employee.update')")
    @RequirePermission("employee.update")
    @Operation(summary = "Cập nhật hoặc thiết lập tài khoản ngân hàng nhận lương")
    public ResponseEntity<ApiResponse<BankAccountResponse>> updateBankAccount(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        BankAccountResponse result = documentService.updateBankAccount(employeeId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật tài khoản ngân hàng thành công"));
    }
}


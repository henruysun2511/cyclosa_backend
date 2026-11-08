package com.cyclosa.contract.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.contract.dto.ContractAppendixDto;
import com.cyclosa.contract.service.ContractAppendixService;
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
@Tag(name = "Contract Appendices", description = "Quản lý phụ lục hợp đồng lao động")
public class ContractAppendixController {

    private final ContractAppendixService appendixService;

    @GetMapping("/contracts/{contractId}/appendices")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy danh sách phụ lục của hợp đồng")
    public ResponseEntity<ApiResponse<List<ContractAppendixDto.ContractAppendixResponse>>> getAppendices(
            @PathVariable UUID contractId
    ) {
        List<ContractAppendixDto.ContractAppendixResponse> result = appendixService.getAppendices(contractId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách phụ lục thành công"));
    }

    @PostMapping("/contracts/{contractId}/appendices")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Tạo mới phụ lục hợp đồng")
    public ResponseEntity<ApiResponse<ContractAppendixDto.ContractAppendixResponse>> createAppendix(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractAppendixDto.CreateAppendixRequest request
    ) {
        ContractAppendixDto.ContractAppendixResponse result = appendixService.createAppendix(contractId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo phụ lục hợp đồng thành công"));
    }

    @GetMapping("/contract-appendices/{appendixId}")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Xem chi tiết phụ lục hợp đồng")
    public ResponseEntity<ApiResponse<ContractAppendixDto.ContractAppendixResponse>> getAppendixById(
            @PathVariable UUID appendixId
    ) {
        ContractAppendixDto.ContractAppendixResponse result = appendixService.getAppendixById(appendixId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy chi tiết phụ lục thành công"));
    }

    @PutMapping("/contract-appendices/{appendixId}")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Chỉnh sửa nội dung phụ lục (khi DRAFT)")
    public ResponseEntity<ApiResponse<ContractAppendixDto.ContractAppendixResponse>> updateAppendix(
            @PathVariable UUID appendixId,
            @Valid @RequestBody ContractAppendixDto.UpdateAppendixRequest request
    ) {
        ContractAppendixDto.ContractAppendixResponse result = appendixService.updateAppendix(appendixId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật phụ lục thành công"));
    }

    @PostMapping("/contract-appendices/{appendixId}/submit")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Trình duyệt nội bộ phụ lục hợp đồng")
    public ResponseEntity<ApiResponse<Void>> submitAppendix(@PathVariable UUID appendixId) {
        appendixService.submitAppendix(appendixId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã trình duyệt phụ lục thành công"));
    }

    @PostMapping("/contract-appendices/{appendixId}/send-for-signature")
    @PreAuthorize("@perm.has('contract.sign_manage')")
    @RequirePermission("contract.sign_manage")
    @Operation(summary = "Phát hành bản ký phụ lục")
    public ResponseEntity<ApiResponse<Void>> sendForSignature(@PathVariable UUID appendixId) {
        appendixService.sendForSignature(appendixId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã phát hành phụ lục sang giai đoạn ký kết"));
    }

    @PostMapping("/contract-appendices/{appendixId}/cancel")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Hủy phụ lục hợp đồng")
    public ResponseEntity<ApiResponse<Void>> cancelAppendix(@PathVariable UUID appendixId) {
        appendixService.cancelAppendix(appendixId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã hủy phụ lục hợp đồng thành công"));
    }

    @PostMapping("/contract-appendices/{appendixId}/archive")
    @PreAuthorize("@perm.has('contract.archive')")
    @RequirePermission("contract.archive")
    @Operation(summary = "Đồng bộ phụ lục đã ký vào kho lưu trữ số")
    public ResponseEntity<ApiResponse<Void>> archiveAppendix(@PathVariable UUID appendixId) {
        appendixService.archiveAppendix(appendixId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã lưu trữ phụ lục vào kho tài liệu"));
    }
}


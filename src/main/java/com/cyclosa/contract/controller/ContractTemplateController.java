package com.cyclosa.contract.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.contract.dto.request.ContractTemplateRequest;
import com.cyclosa.contract.dto.response.ContractTemplateResponse;
import com.cyclosa.contract.service.ContractTemplateService;
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
@RequestMapping("/api/v1/contract-templates")
@RequiredArgsConstructor
@Tag(name = "Contract Templates", description = "Quản lý kho mẫu hợp đồng động và cấu hình biến Mail-Merge")
public class ContractTemplateController {

    private final ContractTemplateService templateService;

    @PostMapping
    @PreAuthorize("@perm.has('contract.config')")
    @RequirePermission("contract.config")
    @Operation(summary = "Tạo mới mẫu hợp đồng lao động chứa biến giữ chỗ {{...}}")
    public ResponseEntity<ApiResponse<ContractTemplateResponse>> createTemplate(
            @Valid @RequestBody ContractTemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(templateService.createTemplate(request), "Tạo mẫu hợp đồng thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy thông tin chi tiết mẫu hợp đồng theo ID")
    public ResponseEntity<ApiResponse<ContractTemplateResponse>> getTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getTemplateById(id), "Lấy thông tin mẫu hợp đồng thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy danh sách các mẫu hợp đồng đang kích hoạt của công ty")
    public ResponseEntity<ApiResponse<List<ContractTemplateResponse>>> getActiveTemplates(
            @RequestParam UUID companyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getActiveTemplates(companyId), "Lấy danh sách mẫu hợp đồng thành công"));
    }
}

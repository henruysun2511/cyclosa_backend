package com.cyclosa.contract.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.contract.dto.ContractTemplateDto;
import com.cyclosa.contract.entity.enums.TemplateKind;
import com.cyclosa.contract.service.ContractTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Contract Templates", description = "Quản lý mẫu biểu hợp đồng lao động & phụ lục")
public class ContractTemplateController {

    private final ContractTemplateService templateService;

    @GetMapping("/contract-templates")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Danh sách mẫu hợp đồng phân trang")
    public ResponseEntity<ApiResponse<PageData<ContractTemplateDto.TemplateResponse>>> getTemplates(
            @RequestParam UUID companyId,
            @RequestParam(required = false) TemplateKind templateKind,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageData<ContractTemplateDto.TemplateResponse> result = templateService.getTemplates(
                companyId, templateKind, active, PageRequest.of(Math.max(0, page), Math.max(1, size))
        );
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách mẫu hợp đồng thành công"));
    }

    @PostMapping("/contract-templates")
    @PreAuthorize("@perm.has('contract.template_manage')")
    @RequirePermission("contract.template_manage")
    @Operation(summary = "Tạo mới mẫu hợp đồng")
    public ResponseEntity<ApiResponse<ContractTemplateDto.TemplateResponse>> createTemplate(
            @Valid @RequestBody ContractTemplateDto.CreateTemplateRequest request
    ) {
        ContractTemplateDto.TemplateResponse result = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mẫu hợp đồng thành công"));
    }

    @GetMapping("/contract-templates/{templateId}")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Xem chi tiết mẫu hợp đồng")
    public ResponseEntity<ApiResponse<ContractTemplateDto.TemplateResponse>> getTemplateById(
            @PathVariable UUID templateId
    ) {
        ContractTemplateDto.TemplateResponse result = templateService.getTemplateById(templateId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy chi tiết mẫu hợp đồng thành công"));
    }

    @PutMapping("/contract-templates/{templateId}")
    @PreAuthorize("@perm.has('contract.template_manage')")
    @RequirePermission("contract.template_manage")
    @Operation(summary = "Cập nhật thuộc tính áp dụng của mẫu hợp đồng")
    public ResponseEntity<ApiResponse<ContractTemplateDto.TemplateResponse>> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody ContractTemplateDto.UpdateTemplateRequest request
    ) {
        ContractTemplateDto.TemplateResponse result = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật mẫu hợp đồng thành công"));
    }

    @PostMapping("/contract-templates/{templateId}/deactivate")
    @PreAuthorize("@perm.has('contract.template_manage')")
    @RequirePermission("contract.template_manage")
    @Operation(summary = "Ngừng sử dụng mẫu hợp đồng")
    public ResponseEntity<ApiResponse<Void>> deactivateTemplate(
            @PathVariable UUID templateId
    ) {
        templateService.deactivateTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã ngừng sử dụng mẫu hợp đồng"));
    }

    @GetMapping("/contract-templates/{templateId}/versions")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy danh sách các phiên bản biểu mẫu")
    public ResponseEntity<ApiResponse<List<ContractTemplateDto.TemplateVersionResponse>>> getVersions(
            @PathVariable UUID templateId
    ) {
        List<ContractTemplateDto.TemplateVersionResponse> result = templateService.getVersions(templateId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách phiên bản mẫu thành công"));
    }

    @PostMapping("/contract-templates/{templateId}/versions")
    @PreAuthorize("@perm.has('contract.template_manage')")
    @RequirePermission("contract.template_manage")
    @Operation(summary = "Tải lên phiên bản tệp mẫu mới")
    public ResponseEntity<ApiResponse<ContractTemplateDto.TemplateVersionResponse>> createVersion(
            @PathVariable UUID templateId,
            @Valid @RequestBody ContractTemplateDto.CreateVersionRequest request
    ) {
        ContractTemplateDto.TemplateVersionResponse result = templateService.createVersion(templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tải lên phiên bản mẫu mới thành công"));
    }

    @GetMapping("/contract-template-versions/{versionId}/download")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Tải tệp biểu mẫu")
    public ResponseEntity<ApiResponse<Map<String, String>>> downloadTemplate(
            @PathVariable UUID versionId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("downloadUrl", "https://storage.cyclosa.vn/templates/contract-v" + versionId + ".docx"),
                "Lấy đường dẫn tải tệp mẫu thành công"
        ));
    }
}


package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CompanyFilterRequest;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyResponse;
import com.cyclosa.organization.service.CompanyService;
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
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Quản lý công ty và pháp nhân")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách công ty có phân trang và bộ lọc", description = "Hỗ trợ lọc từ khóa, trạng thái và phân trang")
    public ResponseEntity<ApiResponse<PageData<CompanyResponse>>> getCompanies(@ParameterObject @Valid CompanyFilterRequest filter) {
        PageData<CompanyResponse> result = companyService.getCompanies(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách công ty thành công"));
    }

    @GetMapping("/{companyId}")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy thông tin chi tiết một công ty theo ID")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(
            @Parameter(description = "ID công ty", required = true)
            @PathVariable UUID companyId) {
        CompanyResponse result = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin công ty thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Tạo mới công ty / pháp nhân")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse result = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mới công ty thành công"));
    }

    @PutMapping("/{companyId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Cập nhật thông tin công ty / pháp nhân")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @Parameter(description = "ID công ty", required = true)
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request) {
        CompanyResponse result = companyService.updateCompany(companyId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật thông tin công ty thành công"));
    }

    @DeleteMapping("/{companyId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Xóa mềm công ty / pháp nhân")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(
            @Parameter(description = "ID công ty", required = true)
            @PathVariable UUID companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa công ty thành công"));
    }
}

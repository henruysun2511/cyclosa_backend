package com.cyclosa.organization.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "01. Cơ cấu tổ chức - Pháp nhân & Công ty", description = "APIs quản lý công ty và pháp nhân")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "Lấy danh sách công ty có phân trang và bộ lọc")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<PageData<CompanyResponse>> getCompanies(@ParameterObject @Valid CompanyFilterRequest filter) {
        return ApiResponse.ok(companyService.getCompanies(filter));
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Lấy thông tin chi tiết một công ty theo ID")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<CompanyResponse> getCompanyById(
            @Parameter(description = "ID công ty", required = true)
            @PathVariable UUID companyId) {
        return ApiResponse.ok(companyService.getCompanyById(companyId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới công ty / pháp nhân")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return ApiResponse.created(companyService.createCompany(request));
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Cập nhật thông tin công ty / pháp nhân")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<CompanyResponse> updateCompany(
            @Parameter(description = "ID công ty", required = true)
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request) {
        return ApiResponse.ok(companyService.updateCompany(companyId, request));
    }

    @DeleteMapping("/{companyId}")
    @Operation(summary = "Xóa mềm công ty / pháp nhân")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> deleteCompany(
            @Parameter(description = "ID công ty", required = true)
            @PathVariable UUID companyId) {
        companyService.deleteCompany(companyId);
        return ApiResponse.ok(null);
    }
}


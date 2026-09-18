package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CompanyFilter;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyDetailResponse;
import com.cyclosa.organization.dto.response.CompanyResponse;
import com.cyclosa.organization.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Tìm kiếm và phân trang danh sách công ty")
    public ResponseEntity<ApiResponse<PageData<CompanyResponse>>> getCompanies(
            @ModelAttribute CompanyFilter filter
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                companyService.getCompanies(filter),
                "Lấy danh sách công ty thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('organization.view')")
    @RequirePermission("organization.view")
    @Operation(summary = "Lấy chi tiết thông tin công ty theo ID")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompanyById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                companyService.getCompanyById(id),
                "Lấy thông tin công ty thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('organization.manage')")
    @RequirePermission("organization.manage")
    @Operation(summary = "Tạo mới công ty / pháp nhân")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        companyService.createCompany(request),
                        "Tạo mới công ty thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('organization.manage')")
    @RequirePermission("organization.manage")
    @Operation(summary = "Cập nhật thông tin công ty")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                companyService.updateCompany(id, request),
                "Cập nhật thông tin công ty thành công"));
    }
}

package com.cyclosa.payroll.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.payroll.dto.request.CreateSalaryComponentRequest;
import com.cyclosa.payroll.dto.request.SalaryComponentFilter;
import com.cyclosa.payroll.dto.request.UpdateSalaryComponentRequest;
import com.cyclosa.payroll.dto.response.SalaryComponentDetailResponse;
import com.cyclosa.payroll.dto.response.SalaryComponentResponse;
import com.cyclosa.payroll.service.SalaryComponentService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salary-components")
@RequiredArgsConstructor
@Tag(name = "Salary Components", description = "Quản lý danh mục thành phần lương (phụ cấp, thưởng, khấu trừ)")
public class SalaryComponentController {

    private final SalaryComponentService componentService;

    @GetMapping
    @PreAuthorize("@perm.has('payroll.config')")
    @RequirePermission("payroll.config")
    @Operation(summary = "Xem danh sách thành phần lương của công ty")
    public ResponseEntity<ApiResponse<PageData<SalaryComponentResponse>>> getComponents(
            @ModelAttribute SalaryComponentFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                componentService.getComponents(headerCompanyId, filter, pageable),
                "Lấy danh sách thành phần lương thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('payroll.config')")
    @RequirePermission("payroll.config")
    @Operation(summary = "Xem chi tiết thành phần lương")
    public ResponseEntity<ApiResponse<SalaryComponentDetailResponse>> getComponentById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                componentService.getComponentById(headerCompanyId, id),
                "Lấy thông tin thành phần lương thành công"
        ));
    }

    @PostMapping
    @PreAuthorize("@perm.has('payroll.config')")
    @RequirePermission("payroll.config")
    @Operation(summary = "Tạo mới thành phần lương")
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> createComponent(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateSalaryComponentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        componentService.createComponent(headerCompanyId, request),
                        "Tạo thành phần lương thành công"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('payroll.config')")
    @RequirePermission("payroll.config")
    @Operation(summary = "Cập nhật thành phần lương")
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> updateComponent(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdateSalaryComponentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                componentService.updateComponent(headerCompanyId, id, request),
                "Cập nhật thành phần lương thành công"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('payroll.config')")
    @RequirePermission("payroll.config")
    @Operation(summary = "Xóa thành phần lương")
    public ResponseEntity<ApiResponse<Void>> deleteComponent(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        componentService.deleteComponent(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa thành phần lương thành công"));
    }
}

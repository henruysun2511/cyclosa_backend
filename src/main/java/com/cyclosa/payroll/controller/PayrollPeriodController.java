package com.cyclosa.payroll.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.payroll.dto.request.CreatePayrollPeriodRequest;
import com.cyclosa.payroll.dto.request.PayrollPeriodFilter;
import com.cyclosa.payroll.dto.request.ProcessPayrollRequest;
import com.cyclosa.payroll.dto.request.UpdatePayrollPeriodRequest;
import com.cyclosa.payroll.dto.response.PayrollPeriodDetailResponse;
import com.cyclosa.payroll.dto.response.PayrollPeriodResponse;
import com.cyclosa.payroll.service.PayrollPeriodService;
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
@RequestMapping("/api/v1/payroll-periods")
@RequiredArgsConstructor
@Tag(name = "Payroll Periods", description = "Quản lý chu kỳ tính lương, chạy tính toán và phê duyệt bảng lương")
public class PayrollPeriodController {

    private final PayrollPeriodService periodService;

    @GetMapping
    @PreAuthorize("@perm.has('payroll.view')")
    @RequirePermission("payroll.view")
    @Operation(summary = "Danh sách các kỳ tính lương")
    public ResponseEntity<ApiResponse<PageData<PayrollPeriodResponse>>> getPeriods(
            @ModelAttribute PayrollPeriodFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                periodService.getPeriods(headerCompanyId, filter, pageable),
                "Lấy danh sách kỳ tính lương thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('payroll.view')")
    @RequirePermission("payroll.view")
    @Operation(summary = "Xem chi tiết kỳ lương và số liệu tổng hợp")
    public ResponseEntity<ApiResponse<PayrollPeriodDetailResponse>> getPeriodById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                periodService.getPeriodById(headerCompanyId, id),
                "Lấy thông tin chi tiết kỳ lương thành công"
        ));
    }

    @PostMapping
    @PreAuthorize("@perm.has('payroll.manage')")
    @RequirePermission("payroll.manage")
    @Operation(summary = "Mở kỳ tính lương mới")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> createPeriod(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreatePayrollPeriodRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        periodService.createPeriod(headerCompanyId, request),
                        "Tạo kỳ tính lương thành công"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('payroll.manage')")
    @RequirePermission("payroll.manage")
    @Operation(summary = "Cập nhật thông tin kỳ tính lương")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> updatePeriod(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdatePayrollPeriodRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                periodService.updatePeriod(headerCompanyId, id, request),
                "Cập nhật kỳ tính lương thành công"
        ));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("@perm.has('payroll.process')")
    @RequirePermission("payroll.process")
    @Operation(summary = "Khởi chạy tính toán tự động toàn bộ nhân viên trong kỳ lương")
    public ResponseEntity<ApiResponse<PayrollPeriodDetailResponse>> processPayroll(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @RequestBody(required = false) ProcessPayrollRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                periodService.processPayroll(headerCompanyId, id, request),
                "Tính toán bảng lương kỳ thành công"
        ));
    }

    @PostMapping("/{id}/submit-approval")
    @PreAuthorize("@perm.has('payroll.manage')")
    @RequirePermission("payroll.manage")
    @Operation(summary = "Trình bảng lương sang Workflow Engine để Giám đốc duyệt")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> submitApproval(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                periodService.submitApproval(headerCompanyId, id),
                "Trình duyệt bảng lương thành công"
        ));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("@perm.has('payroll.approve')")
    @RequirePermission("payroll.approve")
    @Operation(summary = "Ban Giám đốc phê duyệt kỳ lương")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> approvePeriod(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                periodService.approvePeriod(headerCompanyId, id),
                "Phê duyệt bảng lương thành công"
        ));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("@perm.has('payroll.manage')")
    @RequirePermission("payroll.manage")
    @Operation(summary = "Chốt khóa sổ kỳ lương và chuyển trạng thái chi trả")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> closePeriod(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                periodService.closePeriod(headerCompanyId, id),
                "Khóa sổ kỳ lương thành công"
        ));
    }
}

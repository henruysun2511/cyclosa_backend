package com.cyclosa.payroll.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.payroll.dto.request.AdjustPayrollRecordRequest;
import com.cyclosa.payroll.dto.request.PayrollRecordFilter;
import com.cyclosa.payroll.dto.response.PayrollRecordDetailResponse;
import com.cyclosa.payroll.dto.response.PayrollRecordResponse;
import com.cyclosa.payroll.dto.response.PayslipResponse;
import com.cyclosa.payroll.service.PayrollRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll-records")
@RequiredArgsConstructor
@Tag(name = "Payroll Records", description = "Quản lý bảng lương nhân sự, phiếu lương cá nhân (Payslip) và điều chỉnh khoản mục")
public class PayrollRecordController {

    private final PayrollRecordService recordService;

    @GetMapping
    @PreAuthorize("@perm.has('payroll.view')")
    @RequirePermission("payroll.view")
    @Operation(summary = "Danh sách bảng lương nhân sự theo kỳ lương (DataScope)")
    public ResponseEntity<ApiResponse<PageData<PayrollRecordResponse>>> getPayrollRecords(
            @ModelAttribute PayrollRecordFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getPayrollRecords(headerCompanyId, filter, pageable),
                "Lấy danh sách bảng lương thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('payroll.view')")
    @RequirePermission("payroll.view")
    @Operation(summary = "Xem chi tiết toàn diện bản ghi lương của nhân sự")
    public ResponseEntity<ApiResponse<PayrollRecordDetailResponse>> getPayrollRecordById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getPayrollRecordById(headerCompanyId, id),
                "Lấy chi tiết bảng lương thành công"
        ));
    }

    @GetMapping("/my-payslips")
    @Operation(summary = "Nhân viên xem danh sách phiếu lương cá nhân của mình")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getMyPayslips() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getMyPayslips(currentUserId),
                "Lấy danh sách phiếu lương cá nhân thành công"
        ));
    }

    @PutMapping("/{id}/adjust")
    @PreAuthorize("@perm.has('payroll.process')")
    @RequirePermission("payroll.process")
    @Operation(summary = "Kế toán điều chỉnh hoặc bổ sung khoản mục lương trên bản ghi")
    public ResponseEntity<ApiResponse<PayrollRecordDetailResponse>> adjustPayrollRecord(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody AdjustPayrollRecordRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.adjustPayrollRecord(headerCompanyId, id, request),
                "Điều chỉnh bản ghi lương thành công"
        ));
    }
}

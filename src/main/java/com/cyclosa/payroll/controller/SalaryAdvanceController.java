package com.cyclosa.payroll.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.payroll.dto.request.CreateSalaryAdvanceRequest;
import com.cyclosa.payroll.dto.request.SalaryAdvanceFilter;
import com.cyclosa.payroll.dto.response.SalaryAdvanceDetailResponse;
import com.cyclosa.payroll.dto.response.SalaryAdvanceResponse;
import com.cyclosa.payroll.service.SalaryAdvanceService;
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
@RequestMapping("/api/v1/salary-advances")
@RequiredArgsConstructor
@Tag(name = "Salary Advances", description = "Quản lý đơn xin tạm ứng tiền lương và giải ngân")
public class SalaryAdvanceController {

    private final SalaryAdvanceService advanceService;

    @PostMapping
    @PreAuthorize("@perm.has('payroll.advance')")
    @RequirePermission("payroll.advance")
    @Operation(summary = "Nhân viên tạo đơn xin tạm ứng tiền lương")
    public ResponseEntity<ApiResponse<SalaryAdvanceResponse>> createAdvance(
            @Valid @RequestBody CreateSalaryAdvanceRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        advanceService.createAdvance(currentUserId, request),
                        "Gửi đơn tạm ứng lương thành công"
                ));
    }

    @GetMapping("/my")
    @Operation(summary = "Xem danh sách đơn tạm ứng lương của bản thân")
    public ResponseEntity<ApiResponse<PageData<SalaryAdvanceResponse>>> getMyAdvances(
            @ModelAttribute SalaryAdvanceFilter filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                advanceService.getMyAdvances(currentUserId, filter, pageable),
                "Lấy danh sách đơn tạm ứng cá nhân thành công"
        ));
    }

    @GetMapping
    @PreAuthorize("@perm.has('payroll.view')")
    @RequirePermission("payroll.view")
    @Operation(summary = "Tra cứu danh sách đơn tạm ứng lương toàn đơn vị (DataScope)")
    public ResponseEntity<ApiResponse<PageData<SalaryAdvanceResponse>>> getAdvances(
            @ModelAttribute SalaryAdvanceFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                advanceService.getAdvances(headerCompanyId, filter, pageable),
                "Lấy danh sách đơn tạm ứng thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('payroll.view')")
    @RequirePermission("payroll.view")
    @Operation(summary = "Xem chi tiết đơn tạm ứng và tiến trình phê duyệt")
    public ResponseEntity<ApiResponse<SalaryAdvanceDetailResponse>> getAdvanceById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                advanceService.getAdvanceById(headerCompanyId, id),
                "Lấy thông tin đơn tạm ứng thành công"
        ));
    }

    @PutMapping("/{id}/disburse")
    @PreAuthorize("@perm.has('payroll.process')")
    @RequirePermission("payroll.process")
    @Operation(summary = "Kế toán giải ngân chi trả khoản tạm ứng lương")
    public ResponseEntity<ApiResponse<SalaryAdvanceResponse>> disburseAdvance(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                advanceService.disburseAdvance(headerCompanyId, id),
                "Giải ngân tạm ứng lương thành công"
        ));
    }
}

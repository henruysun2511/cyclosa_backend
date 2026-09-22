package com.cyclosa.leave.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.leave.dto.request.LeaveBalanceFilter;
import com.cyclosa.leave.dto.request.RecalculateLeaveBalanceRequest;
import com.cyclosa.leave.dto.response.LeaveBalanceResponse;
import com.cyclosa.leave.dto.response.UnusedLeavePayoutResponse;
import com.cyclosa.leave.service.LeaveBalanceService;
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
@RequestMapping("/api/v1/leave-balances")
@RequiredArgsConstructor
@Tag(name = "Leave Balances", description = "Quản lý quỹ phép năm, số dư phép khả dụng và thanh toán phép tồn")
public class LeaveBalanceController {

    private final LeaveBalanceService balanceService;

    @GetMapping("/my")
    @Operation(summary = "Nhân viên tự tra cứu số dư quỹ phép trong năm của bản thân (ESS)")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getMyBalances(
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                balanceService.getMyBalances(year),
                "Tra cứu số dư phép cá nhân thành công"
        ));
    }

    @GetMapping
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Tra cứu danh sách quỹ phép nhân viên theo phân quyền dữ liệu (DataScope)")
    public ResponseEntity<ApiResponse<PageData<LeaveBalanceResponse>>> getBalances(
            @ModelAttribute LeaveBalanceFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                balanceService.getBalances(headerCompanyId, filter, pageable),
                "Lấy danh sách quỹ phép thành công"
        ));
    }

    @PostMapping("/recalculate")
    @PreAuthorize("@perm.has('leave.manage')")
    @RequirePermission("leave.manage")
    @Operation(summary = "Khởi chạy tính toán lại quỹ phép năm per nhân sự theo quy định BLLĐ 2019")
    public ResponseEntity<ApiResponse<Integer>> recalculateBalances(
            @Valid @RequestBody RecalculateLeaveBalanceRequest request,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        int count = balanceService.recalculateBalances(headerCompanyId, request);
        return ResponseEntity.ok(ApiResponse.ok(count, String.format("Đã tính toán lại quỹ phép cho %d nhân sự", count)));
    }

    @GetMapping("/{employeeId}/unused-annual-leave-payout")
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Tính toán số tiền thanh toán ngày phép năm chưa nghỉ khi thôi việc (Điều 113.3 BLLĐ)")
    public ResponseEntity<ApiResponse<UnusedLeavePayoutResponse>> calculateUnusedAnnualLeavePayout(
            @PathVariable UUID employeeId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                balanceService.calculateUnusedAnnualLeavePayout(headerCompanyId, employeeId),
                "Tính thanh toán tiền phép năm chưa sử dụng thành công"
        ));
    }
}

package com.cyclosa.attendance.controller;

import com.cyclosa.attendance.dto.request.LockTimesheetRequest;
import com.cyclosa.attendance.dto.request.RecalculateTimesheetRequest;
import com.cyclosa.attendance.dto.request.TimesheetFilter;
import com.cyclosa.attendance.dto.response.MonthlyTimesheetResponse;
import com.cyclosa.attendance.service.TimesheetService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/timesheets")
@RequiredArgsConstructor
@Tag(name = "Timesheets", description = "Tổng hợp bảng công tháng, chốt khóa công và cung cấp dữ liệu cho Module Payroll")
public class TimesheetController {

    private final TimesheetService timesheetService;

    @GetMapping("/my-timesheet")
    @PreAuthorize("@perm.has('attendance.view_own')")
    @RequirePermission("attendance.view_own")
    @Operation(summary = "Xem bảng công tổng hợp cá nhân tháng này")
    public ResponseEntity<ApiResponse<MonthlyTimesheetResponse>> getMyTimesheet(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.ok(
                timesheetService.getMyTimesheet(m, y),
                "Lấy bảng công cá nhân thành công"
        ));
    }

    @GetMapping("/summary")
    @PreAuthorize("@perm.has('attendance.timesheet.view')")
    @RequirePermission("attendance.timesheet.view")
    @Operation(summary = "Bảng tổng hợp công theo phòng ban/công ty (DataScope)")
    public ResponseEntity<ApiResponse<PageData<MonthlyTimesheetResponse>>> getTimesheetSummary(
            @ModelAttribute TimesheetFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                timesheetService.getTimesheetSummary(headerCompanyId, filter, pageable),
                "Lấy bảng công tổng hợp thành công"
        ));
    }

    @PostMapping("/recalculate")
    @PreAuthorize("@perm.has('attendance.timesheet.manage')")
    @RequirePermission("attendance.timesheet.manage")
    @Operation(summary = "Chạy lệnh tính toán lại bảng công tháng")
    public ResponseEntity<ApiResponse<List<MonthlyTimesheetResponse>>> recalculateTimesheets(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody RecalculateTimesheetRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                timesheetService.recalculateTimesheets(headerCompanyId, request),
                "Tính toán lại bảng công tháng thành công"
        ));
    }

    @PostMapping("/lock")
    @PreAuthorize("@perm.has('attendance.timesheet.lock')")
    @RequirePermission("attendance.timesheet.lock")
    @Operation(summary = "Chốt khóa bảng công tháng (bảo vệ snapshot cho kế toán tính lương)")
    public ResponseEntity<ApiResponse<Void>> lockTimesheet(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody LockTimesheetRequest request
    ) {
        timesheetService.lockTimesheet(headerCompanyId, request);
        return ResponseEntity.ok(ApiResponse.noContent("Khóa bảng công tháng thành công"));
    }
}

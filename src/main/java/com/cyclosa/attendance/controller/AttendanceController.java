package com.cyclosa.attendance.controller;

import com.cyclosa.attendance.dto.request.AttendanceRecordFilter;
import com.cyclosa.attendance.dto.request.CheckInRequest;
import com.cyclosa.attendance.dto.request.CheckOutRequest;
import com.cyclosa.attendance.dto.response.AttendanceRecordResponse;
import com.cyclosa.attendance.dto.response.AttendanceTodayResponse;
import com.cyclosa.attendance.service.AttendanceRecordService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
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
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Chấm công điểm danh Check-in / Check-out Geofencing GPS & nhật ký điểm danh")
public class AttendanceController {

    private final AttendanceRecordService recordService;

    @PostMapping("/check-in")
    @PreAuthorize("@perm.has('attendance.record')")
    @RequirePermission("attendance.record")
    @Operation(summary = "Thực hiện Check-in vào ca (gửi kèm tọa độ GPS)")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> checkIn(
            @Valid @RequestBody CheckInRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.checkIn(currentUserId, request),
                "Check-in thành công"
        ));
    }

    @PostMapping("/check-out")
    @PreAuthorize("@perm.has('attendance.record')")
    @RequirePermission("attendance.record")
    @Operation(summary = "Thực hiện Check-out ra ca (gửi kèm tọa độ GPS)")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> checkOut(
            @Valid @RequestBody CheckOutRequest request
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.checkOut(currentUserId, request),
                "Check-out thành công"
        ));
    }

    @GetMapping("/today")
    @PreAuthorize("@perm.has('attendance.view_own')")
    @RequirePermission("attendance.view_own")
    @Operation(summary = "Lấy trạng thái điểm danh hôm nay của cá nhân")
    public ResponseEntity<ApiResponse<AttendanceTodayResponse>> getTodayAttendance() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getTodayAttendance(currentUserId),
                "Lấy trạng thái điểm danh hôm nay thành công"
        ));
    }

    @GetMapping("/history")
    @PreAuthorize("@perm.has('attendance.view_own')")
    @RequirePermission("attendance.view_own")
    @Operation(summary = "Tra cứu lịch sử chấm công cá nhân theo tháng/năm")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getMyAttendanceHistory(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getMyAttendanceHistory(currentUserId, m, y),
                "Lấy lịch sử chấm công thành công"
        ));
    }

    @GetMapping("/records")
    @PreAuthorize("@perm.has('attendance.record.view')")
    @RequirePermission("attendance.record.view")
    @Operation(summary = "Quản lý tra cứu dữ liệu chấm công toàn đơn vị theo DataScope")
    public ResponseEntity<ApiResponse<PageData<AttendanceRecordResponse>>> getAttendanceRecords(
            @ModelAttribute AttendanceRecordFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getAttendanceRecords(headerCompanyId, filter, pageable),
                "Lấy danh sách nhật ký chấm công thành công"
        ));
    }
}

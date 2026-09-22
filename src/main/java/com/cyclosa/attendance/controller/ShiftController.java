package com.cyclosa.attendance.controller;

import com.cyclosa.attendance.dto.request.CreateShiftRequest;
import com.cyclosa.attendance.dto.request.ShiftFilter;
import com.cyclosa.attendance.dto.request.UpdateShiftRequest;
import com.cyclosa.attendance.dto.response.ShiftResponse;
import com.cyclosa.attendance.service.ShiftService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
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
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts", description = "Quản lý danh mục ca làm việc (chuẩn thời gian, nghỉ giữa ca, mức công)")
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    @PreAuthorize("@perm.has('attendance.shift.view')")
    @RequirePermission("attendance.shift.view")
    @Operation(summary = "Xem danh sách ca làm việc của công ty")
    public ResponseEntity<ApiResponse<PageData<ShiftResponse>>> getShifts(
            @ModelAttribute ShiftFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                shiftService.getShifts(headerCompanyId, filter, pageable),
                "Lấy danh sách ca làm việc thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('attendance.shift.view')")
    @RequirePermission("attendance.shift.view")
    @Operation(summary = "Xem chi tiết ca làm việc")
    public ResponseEntity<ApiResponse<com.cyclosa.attendance.dto.response.ShiftDetailResponse>> getShiftById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                shiftService.getShiftById(headerCompanyId, id),
                "Lấy thông tin ca làm việc thành công"
        ));
    }

    @PostMapping
    @PreAuthorize("@perm.has('attendance.shift.create')")
    @RequirePermission("attendance.shift.create")
    @Operation(summary = "Tạo mới ca làm việc")
    public ResponseEntity<ApiResponse<ShiftResponse>> createShift(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateShiftRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        shiftService.createShift(headerCompanyId, request),
                        "Tạo ca làm việc thành công"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('attendance.shift.update')")
    @RequirePermission("attendance.shift.update")
    @Operation(summary = "Cập nhật ca làm việc")
    public ResponseEntity<ApiResponse<ShiftResponse>> updateShift(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody UpdateShiftRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                shiftService.updateShift(headerCompanyId, id, request),
                "Cập nhật ca làm việc thành công"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('attendance.shift.delete')")
    @RequirePermission("attendance.shift.delete")
    @Operation(summary = "Xóa ca làm việc")
    public ResponseEntity<ApiResponse<Void>> deleteShift(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        shiftService.deleteShift(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa ca làm việc thành công"));
    }
}

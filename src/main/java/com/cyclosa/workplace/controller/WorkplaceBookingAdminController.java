package com.cyclosa.workplace.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.workplace.dto.filter.HotdeskBookingFilter;
import com.cyclosa.workplace.dto.filter.RoomBookingFilter;
import com.cyclosa.workplace.dto.request.CreateHotdeskBookingRequest;
import com.cyclosa.workplace.dto.request.CreateRoomBookingRequest;
import com.cyclosa.workplace.dto.response.HotdeskBookingResponse;
import com.cyclosa.workplace.dto.response.RoomAvailabilityResponse;
import com.cyclosa.workplace.dto.response.RoomBookingResponse;
import com.cyclosa.workplace.service.WorkplaceBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workplace/bookings")
@RequiredArgsConstructor
@Tag(name = "Workplace Bookings", description = "Quản lý đặt bàn linh hoạt (Hotdesk) và đặt phòng họp")
public class WorkplaceBookingAdminController {

    private final WorkplaceBookingService bookingService;

    // =========================================================================
    // HOTDESK BOOKINGS
    // =========================================================================

    @GetMapping("/hotdesk")
    @PreAuthorize("@perm.has('workplace.booking.view')")
    @RequirePermission("workplace.booking.view")
    @Operation(summary = "Lọc danh sách đặt bàn linh hoạt hotdesk")
    public ResponseEntity<ApiResponse<PageData<HotdeskBookingResponse>>> filterHotdeskBookings(@Valid HotdeskBookingFilter filter) {
        PageData<HotdeskBookingResponse> response = bookingService.filterHotdeskBookings(filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/hotdesk")
    @PreAuthorize("@perm.has('workplace.booking.manage')")
    @RequirePermission("workplace.booking.manage")
    @Operation(summary = "Tạo lượt đặt bàn linh hoạt")
    public ResponseEntity<ApiResponse<HotdeskBookingResponse>> createHotdeskBooking(@Valid @RequestBody CreateHotdeskBookingRequest request) {
        HotdeskBookingResponse response = bookingService.createHotdeskBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/hotdesk/{id}/cancel")
    @PreAuthorize("@perm.has('workplace.booking.manage')")
    @RequirePermission("workplace.booking.manage")
    @Operation(summary = "Hủy đặt bàn linh hoạt")
    public ResponseEntity<ApiResponse<HotdeskBookingResponse>> cancelHotdeskBooking(@PathVariable UUID id) {
        HotdeskBookingResponse response = bookingService.cancelHotdeskBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/hotdesk/{id}/check-in")
    @PreAuthorize("@perm.has('workplace.booking.manage')")
    @RequirePermission("workplace.booking.manage")
    @Operation(summary = "Check-in bàn linh hoạt")
    public ResponseEntity<ApiResponse<HotdeskBookingResponse>> checkInHotdeskBooking(@PathVariable UUID id) {
        HotdeskBookingResponse response = bookingService.checkInHotdeskBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // =========================================================================
    // ROOM BOOKINGS
    // =========================================================================

    @GetMapping("/rooms")
    @PreAuthorize("@perm.has('workplace.booking.view')")
    @RequirePermission("workplace.booking.view")
    @Operation(summary = "Lọc danh sách đặt phòng họp")
    public ResponseEntity<ApiResponse<PageData<RoomBookingResponse>>> filterRoomBookings(@Valid RoomBookingFilter filter) {
        PageData<RoomBookingResponse> response = bookingService.filterRoomBookings(filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/rooms")
    @PreAuthorize("@perm.has('workplace.booking.manage')")
    @RequirePermission("workplace.booking.manage")
    @Operation(summary = "Tạo lịch đặt phòng họp mới (có kiểm tra chống trùng lịch)")
    public ResponseEntity<ApiResponse<RoomBookingResponse>> createRoomBooking(@Valid @RequestBody CreateRoomBookingRequest request) {
        RoomBookingResponse response = bookingService.createRoomBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/rooms/{id}/cancel")
    @PreAuthorize("@perm.has('workplace.booking.manage')")
    @RequirePermission("workplace.booking.manage")
    @Operation(summary = "Hủy lịch đặt phòng họp")
    public ResponseEntity<ApiResponse<RoomBookingResponse>> cancelRoomBooking(@PathVariable UUID id) {
        RoomBookingResponse response = bookingService.cancelRoomBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/rooms/{id}/check-in")
    @PreAuthorize("@perm.has('workplace.booking.manage')")
    @RequirePermission("workplace.booking.manage")
    @Operation(summary = "Check-in phòng họp")
    public ResponseEntity<ApiResponse<RoomBookingResponse>> checkInRoomBooking(@PathVariable UUID id) {
        RoomBookingResponse response = bookingService.checkInRoomBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/rooms/{roomId}/availability")
    @PreAuthorize("@perm.has('workplace.booking.view')")
    @RequirePermission("workplace.booking.view")
    @Operation(summary = "Kiểm tra các khung giờ trống và đã được đặt của phòng họp theo ngày")
    public ResponseEntity<ApiResponse<RoomAvailabilityResponse>> getRoomAvailability(
            @PathVariable UUID roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        RoomAvailabilityResponse response = bookingService.getRoomAvailability(roomId, date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

package com.cyclosa.workplace.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.workplace.dto.request.CreateHotdeskBookingRequest;
import com.cyclosa.workplace.dto.request.CreateIssueTicketRequest;
import com.cyclosa.workplace.dto.request.CreateRoomBookingRequest;
import com.cyclosa.workplace.dto.response.*;
import com.cyclosa.workplace.service.MyWorkplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/my-workplace")
@RequiredArgsConstructor
@Tag(name = "My Workplace (Self-Service)", description = "Cổng thông tin tự phục vụ dành cho nhân viên (Tra cứu vị trí bàn làm việc, đặt bàn, đặt phòng họp, báo sự cố)")
public class MyWorkplaceController {

    private final MyWorkplaceService myWorkplaceService;

    // =========================================================================
    // ASSIGNED SEAT
    // =========================================================================

    @GetMapping("/seat")
    @PreAuthorize("@perm.has('my_workplace.view')")
    @RequirePermission("my_workplace.view")
    @Operation(summary = "Xem thông tin chi tiết vị trí chỗ ngồi và danh sách tài sản gắn trên bàn của tôi")
    public ResponseEntity<ApiResponse<WorkplaceSeatDetailResponse>> getMyAssignedSeat() {
        WorkplaceSeatDetailResponse response = myWorkplaceService.getMyAssignedSeat();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // =========================================================================
    // HOTDESK BOOKINGS
    // =========================================================================

    @GetMapping("/hotdesk-bookings")
    @PreAuthorize("@perm.has('my_workplace.view')")
    @RequirePermission("my_workplace.view")
    @Operation(summary = "Xem danh sách các lượt đặt bàn linh hoạt của tôi")
    public ResponseEntity<ApiResponse<List<HotdeskBookingResponse>>> getMyHotdeskBookings() {
        List<HotdeskBookingResponse> response = myWorkplaceService.getMyHotdeskBookings();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/hotdesk-bookings")
    @PreAuthorize("@perm.has('my_workplace.manage')")
    @RequirePermission("my_workplace.manage")
    @Operation(summary = "Tự đặt bàn linh hoạt cho ngày làm việc")
    public ResponseEntity<ApiResponse<HotdeskBookingResponse>> bookHotdesk(@Valid @RequestBody CreateHotdeskBookingRequest request) {
        HotdeskBookingResponse response = myWorkplaceService.bookHotdesk(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/hotdesk-bookings/{id}/cancel")
    @PreAuthorize("@perm.has('my_workplace.manage')")
    @RequirePermission("my_workplace.manage")
    @Operation(summary = "Hủy lượt đặt bàn linh hoạt của tôi")
    public ResponseEntity<ApiResponse<HotdeskBookingResponse>> cancelMyHotdeskBooking(@PathVariable UUID id) {
        HotdeskBookingResponse response = myWorkplaceService.cancelMyHotdeskBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/hotdesk-bookings/{id}/check-in")
    @PreAuthorize("@perm.has('my_workplace.manage')")
    @RequirePermission("my_workplace.manage")
    @Operation(summary = "Check-in bàn linh hoạt của tôi")
    public ResponseEntity<ApiResponse<HotdeskBookingResponse>> checkInMyHotdeskBooking(@PathVariable UUID id) {
        HotdeskBookingResponse response = myWorkplaceService.checkInMyHotdeskBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // =========================================================================
    // ROOM BOOKINGS
    // =========================================================================

    @GetMapping("/room-bookings")
    @PreAuthorize("@perm.has('my_workplace.view')")
    @RequirePermission("my_workplace.view")
    @Operation(summary = "Xem danh sách các lịch đặt phòng họp của tôi")
    public ResponseEntity<ApiResponse<List<RoomBookingResponse>>> getMyRoomBookings() {
        List<RoomBookingResponse> response = myWorkplaceService.getMyRoomBookings();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/room-bookings")
    @PreAuthorize("@perm.has('my_workplace.manage')")
    @RequirePermission("my_workplace.manage")
    @Operation(summary = "Tự đặt phòng họp")
    public ResponseEntity<ApiResponse<RoomBookingResponse>> bookRoom(@Valid @RequestBody CreateRoomBookingRequest request) {
        RoomBookingResponse response = myWorkplaceService.bookRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/room-bookings/{id}/cancel")
    @PreAuthorize("@perm.has('my_workplace.manage')")
    @RequirePermission("my_workplace.manage")
    @Operation(summary = "Hủy lịch đặt phòng họp của tôi")
    public ResponseEntity<ApiResponse<RoomBookingResponse>> cancelMyRoomBooking(@PathVariable UUID id) {
        RoomBookingResponse response = myWorkplaceService.cancelMyRoomBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/room-bookings/{id}/check-in")
    @PreAuthorize("@perm.has('my_workplace.manage')")
    @RequirePermission("my_workplace.manage")
    @Operation(summary = "Check-in phòng họp của tôi")
    public ResponseEntity<ApiResponse<RoomBookingResponse>> checkInMyRoomBooking(@PathVariable UUID id) {
        RoomBookingResponse response = myWorkplaceService.checkInMyRoomBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // =========================================================================
    // FACILITY ISSUE TICKETS
    // =========================================================================

    @PostMapping("/issue-tickets")
    @PreAuthorize("@perm.has('my_workplace.manage')")
    @RequirePermission("my_workplace.manage")
    @Operation(summary = "Báo cáo sự cố cơ sở vật chất tại vị trí bàn hoặc phòng họp")
    public ResponseEntity<ApiResponse<IssueTicketResponse>> reportIssue(@Valid @RequestBody CreateIssueTicketRequest request) {
        IssueTicketResponse response = myWorkplaceService.reportIssue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/issue-tickets")
    @PreAuthorize("@perm.has('my_workplace.view')")
    @RequirePermission("my_workplace.view")
    @Operation(summary = "Xem danh sách các sự cố do tôi báo cáo")
    public ResponseEntity<ApiResponse<List<IssueTicketResponse>>> getMyReportedTickets() {
        List<IssueTicketResponse> response = myWorkplaceService.getMyReportedTickets();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

package com.cyclosa.workplace.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.workplace.dto.request.NavigationRequest;
import com.cyclosa.workplace.dto.request.QrScanRequest;
import com.cyclosa.workplace.dto.response.*;
import com.cyclosa.workplace.service.WorkplaceFloorPlanService;
import com.cyclosa.workplace.service.WorkplaceNavigationService;
import com.cyclosa.workplace.service.WorkplaceQrService;
import com.cyclosa.workplace.service.WorkplaceSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workplace/navigation")
@RequiredArgsConstructor
@Tag(name = "Workplace Navigation & Wayfinding", description = "Định vị 5 cấp, thuật toán tìm đường ngắn nhất A*, quét QR và tìm kiếm vị trí đồng nghiệp")
public class WorkplaceNavigationController {

    private final WorkplaceNavigationService navigationService;
    private final WorkplaceQrService qrService;
    private final WorkplaceSearchService searchService;
    private final WorkplaceFloorPlanService floorPlanService;

    @PostMapping("/route")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Tính toán lộ trình điều hướng nội bộ (Indoor Wayfinding 5 cấp, hỗ trợ thuật toán A* né vật cản)")
    public ResponseEntity<ApiResponse<NavigationRouteResponse>> calculateRoute(@RequestBody NavigationRequest request) {
        NavigationRouteResponse response = navigationService.calculateRoute(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/scan-qr")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Quét mã QR tại bàn hoặc phòng họp để nhận diện vị trí và kích hoạt hành động")
    public ResponseEntity<ApiResponse<QrScanResultResponse>> processQrScan(@Valid @RequestBody QrScanRequest request) {
        QrScanResultResponse response = qrService.processQrScan(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/colleagues")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Tìm kiếm vị trí chỗ ngồi của đồng nghiệp theo tên, mã nhân viên hoặc mã bàn")
    public ResponseEntity<ApiResponse<List<ColleagueLocationResponse>>> findColleagues(@RequestParam(required = false) String search) {
        List<ColleagueLocationResponse> response = searchService.findColleagueLocation(search);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/colleagues/{employeeId}")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Tra cứu vị trí chỗ ngồi chi tiết của một đồng nghiệp cụ thể")
    public ResponseEntity<ApiResponse<ColleagueLocationResponse>> getColleagueLocation(@PathVariable UUID employeeId) {
        ColleagueLocationResponse response = searchService.findColleagueLocationByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/bookable-rooms")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Lấy danh sách các phòng họp có thể đặt")
    public ResponseEntity<ApiResponse<List<WorkplaceNonDeskObjectResponse>>> getBookableRooms(@RequestParam(required = false) UUID floorId) {
        List<WorkplaceNonDeskObjectResponse> response = floorPlanService.getBookableRooms(floorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

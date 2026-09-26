package com.cyclosa.workplace.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.workplace.dto.filter.WorkplaceSeatFilter;
import com.cyclosa.workplace.dto.request.*;
import com.cyclosa.workplace.dto.response.*;
import com.cyclosa.workplace.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workplace")
@RequiredArgsConstructor
@Tag(name = "Workplace Management", description = "Quản lý sơ đồ không gian làm việc, tầng, khu vực, dãy bàn và chỗ ngồi")
public class WorkplaceManagementController {

    private final WorkplaceHierarchyService hierarchyService;
    private final WorkplaceSeatService seatService;
    private final WorkplaceFloorPlanService floorPlanService;
    private final WorkplaceSearchService searchService;
    private final WorkplaceQrService qrService;
    private final WorkplaceSecurityContext securityContext;

    private UUID resolveCompanyId(UUID headerCompanyId) {
        return (headerCompanyId != null) ? headerCompanyId : securityContext.getCurrentCompanyId();
    }

    // =========================================================================
    // SITE MANAGEMENT
    // =========================================================================

    @PostMapping("/sites")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Tạo mới cơ sở/tòa nhà làm việc")
    public ResponseEntity<ApiResponse<WorkplaceSiteResponse>> createSite(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateSiteRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSiteResponse response = hierarchyService.createSite(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/sites/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Cập nhật thông tin cơ sở/tòa nhà làm việc")
    public ResponseEntity<ApiResponse<WorkplaceSiteResponse>> updateSite(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSiteRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSiteResponse response = hierarchyService.updateSite(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/sites")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Lấy danh sách tất cả cơ sở/tòa nhà làm việc")
    public ResponseEntity<ApiResponse<PageData<WorkplaceSiteResponse>>> getAllSites(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        PageData<WorkplaceSiteResponse> response = hierarchyService.getSites(companyId, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/sites/{id}")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Lấy thông tin chi tiết một cơ sở làm việc")
    public ResponseEntity<ApiResponse<WorkplaceSiteResponse>> getSiteById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSiteResponse response = hierarchyService.getSiteById(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/sites/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Xóa cơ sở làm việc")
    public ResponseEntity<ApiResponse<Void>> deleteSite(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        hierarchyService.deleteSite(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // =========================================================================
    // FLOOR MANAGEMENT
    // =========================================================================

    @PostMapping("/floors")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Tạo mới tầng làm việc")
    public ResponseEntity<ApiResponse<WorkplaceFloorResponse>> createFloor(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateFloorRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceFloorResponse response = hierarchyService.createFloor(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/floors/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Cập nhật thông tin tầng làm việc")
    public ResponseEntity<ApiResponse<WorkplaceFloorResponse>> updateFloor(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFloorRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceFloorResponse response = hierarchyService.updateFloor(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/sites/{siteId}/floors")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Lấy danh sách các tầng thuộc một cơ sở")
    public ResponseEntity<ApiResponse<List<WorkplaceFloorResponse>>> getFloorsBySite(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID siteId) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        List<WorkplaceFloorResponse> response = hierarchyService.getFloorsBySite(companyId, siteId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/floors/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Xóa tầng làm việc")
    public ResponseEntity<ApiResponse<Void>> deleteFloor(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        hierarchyService.deleteFloor(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // =========================================================================
    // ZONE MANAGEMENT
    // =========================================================================

    @PostMapping("/zones")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Tạo mới phân khu (Zone)")
    public ResponseEntity<ApiResponse<WorkplaceZoneResponse>> createZone(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateZoneRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceZoneResponse response = hierarchyService.createZone(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/zones/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Cập nhật thông tin phân khu")
    public ResponseEntity<ApiResponse<WorkplaceZoneResponse>> updateZone(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateZoneRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceZoneResponse response = hierarchyService.updateZone(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/floors/{floorId}/zones")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Lấy danh sách các phân khu trên một tầng")
    public ResponseEntity<ApiResponse<List<WorkplaceZoneResponse>>> getZonesByFloor(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID floorId) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        List<WorkplaceZoneResponse> response = hierarchyService.getZonesByFloor(companyId, floorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/zones/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Xóa phân khu")
    public ResponseEntity<ApiResponse<Void>> deleteZone(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        hierarchyService.deleteZone(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // =========================================================================
    // ROW MANAGEMENT
    // =========================================================================

    @PostMapping("/rows")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Tạo mới cụm / dãy bàn (Row/Cluster)")
    public ResponseEntity<ApiResponse<WorkplaceRowResponse>> createRow(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateRowRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceRowResponse response = hierarchyService.createRow(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/rows/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Cập nhật cụm / dãy bàn")
    public ResponseEntity<ApiResponse<WorkplaceRowResponse>> updateRow(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRowRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceRowResponse response = hierarchyService.updateRow(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/zones/{zoneId}/rows")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Lấy danh sách cụm bàn thuộc một phân khu")
    public ResponseEntity<ApiResponse<List<WorkplaceRowResponse>>> getRowsByZone(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID zoneId) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        List<WorkplaceRowResponse> response = hierarchyService.getRowsByZone(companyId, zoneId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/rows/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Xóa cụm bàn")
    public ResponseEntity<ApiResponse<Void>> deleteRow(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        hierarchyService.deleteRow(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // =========================================================================
    // SEAT MANAGEMENT
    // =========================================================================

    @PostMapping("/seats")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Tạo mới một chỗ ngồi làm việc")
    public ResponseEntity<ApiResponse<WorkplaceSeatResponse>> createSeat(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateSeatRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSeatResponse response = seatService.createSeat(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/rows/{rowId}/bulk-generate-seats")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Sinh danh sách bàn tự động theo cụm (Pod 4/6/8 hoặc dãy đơn)")
    public ResponseEntity<ApiResponse<List<WorkplaceSeatResponse>>> bulkGenerateSeats(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID rowId,
            @Valid @RequestBody BulkGenerateSeatsRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        List<WorkplaceSeatResponse> response = seatService.bulkGenerateSeats(companyId, rowId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/seats/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Cập nhật thông tin và tọa độ hình học 2D chỗ ngồi")
    public ResponseEntity<ApiResponse<WorkplaceSeatResponse>> updateSeat(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSeatRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSeatResponse response = seatService.updateSeat(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/seats/{id}")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Xem chi tiết chỗ ngồi (vị trí 5 cấp + danh sách tài sản gắn trên bàn)")
    public ResponseEntity<ApiResponse<WorkplaceSeatDetailResponse>> getSeatDetail(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSeatDetailResponse response = seatService.getSeatDetail(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/seats")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Lọc danh sách chỗ ngồi có phân trang")
    public ResponseEntity<ApiResponse<PageData<WorkplaceSeatResponse>>> filterSeats(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid WorkplaceSeatFilter filter,
            Pageable pageable) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        PageData<WorkplaceSeatResponse> response = seatService.filterSeats(companyId, filter, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/seats/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Xóa chỗ ngồi làm việc")
    public ResponseEntity<ApiResponse<Void>> deleteSeat(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        seatService.deleteSeat(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/seats/{id}/assign")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Gán chỗ ngồi cố định (Dedicated) cho nhân viên")
    public ResponseEntity<ApiResponse<WorkplaceSeatResponse>> assignSeat(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody AssignSeatRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSeatResponse response = seatService.assignSeat(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/seats/{id}/release")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Giải phóng chỗ ngồi (thu hồi gán chỗ)")
    public ResponseEntity<ApiResponse<WorkplaceSeatResponse>> releaseSeat(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSeatResponse response = seatService.releaseSeat(companyId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/seats/{id}/status")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Thay đổi trạng thái chỗ ngồi (Khóa, Bảo trì, Trống...)")
    public ResponseEntity<ApiResponse<WorkplaceSeatResponse>> changeSeatStatus(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody ChangeSeatStatusRequest request) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        WorkplaceSeatResponse response = seatService.changeStatus(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/seats/{id}/link-asset/{assetId}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Gắn tài sản lên bàn làm việc")
    public ResponseEntity<ApiResponse<Void>> linkAsset(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        seatService.linkAsset(companyId, id, assetId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/seats/{id}/unlink-asset/{assetId}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Gỡ liên kết tài sản khỏi bàn làm việc")
    public ResponseEntity<ApiResponse<Void>> unlinkAsset(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        UUID companyId = resolveCompanyId(headerCompanyId);
        seatService.unlinkAsset(companyId, id, assetId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/seats/{id}/qr-code")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Sinh mã QR cho bàn làm việc")
    public ResponseEntity<ApiResponse<String>> generateSeatQr(@PathVariable UUID id) {
        String qr = qrService.generateSeatQrCode(id);
        return ResponseEntity.ok(ApiResponse.ok(qr));
    }

    // =========================================================================
    // 2D CANVAS LAYOUT & FLOOR PLAN
    // =========================================================================

    @GetMapping("/floors/{floorId}/canvas-layout")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Tải toàn bộ dữ liệu 2D Canvas của một tầng (Sơ đồ nền, Zones, Rows, Seats, NonDeskObjects)")
    public ResponseEntity<ApiResponse<FloorLayoutCanvasResponse>> getFloorLayoutCanvas(@PathVariable UUID floorId) {
        FloorLayoutCanvasResponse response = floorPlanService.getFloorLayoutCanvas(floorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/floors/{floorId}/floor-plan")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Cấu hình / Cập nhật Floor Plan nền của tầng")
    public ResponseEntity<ApiResponse<WorkplaceFloorPlanResponse>> saveFloorPlan(@PathVariable UUID floorId,
                                                                                 @Valid @RequestBody CreateFloorPlanRequest request) {
        WorkplaceFloorPlanResponse response = floorPlanService.saveOrUpdateFloorPlan(floorId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // =========================================================================
    // NON-DESK OBJECTS (Meeting room, Elevator, Stairs, WC, Pantry...)
    // =========================================================================

    @PostMapping("/non-desk-objects")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Tạo vật thể không gian (Phòng họp, thang máy, tiện ích...)")
    public ResponseEntity<ApiResponse<WorkplaceNonDeskObjectResponse>> createNonDeskObject(@Valid @RequestBody CreateNonDeskObjectRequest request) {
        WorkplaceNonDeskObjectResponse response = floorPlanService.createNonDeskObject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/non-desk-objects/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Cập nhật vật thể không gian")
    public ResponseEntity<ApiResponse<WorkplaceNonDeskObjectResponse>> updateNonDeskObject(@PathVariable UUID id,
                                                                                           @Valid @RequestBody UpdateNonDeskObjectRequest request) {
        WorkplaceNonDeskObjectResponse response = floorPlanService.updateNonDeskObject(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/non-desk-objects/{id}")
    @PreAuthorize("@perm.has('workplace.manage')")
    @RequirePermission("workplace.manage")
    @Operation(summary = "Xóa vật thể không gian")
    public ResponseEntity<ApiResponse<Void>> deleteNonDeskObject(@PathVariable UUID id) {
        floorPlanService.deleteNonDeskObject(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // =========================================================================
    // ANALYTICS & REPORTS
    // =========================================================================

    @GetMapping("/analytics/occupancy-report")
    @PreAuthorize("@perm.has('workplace.view')")
    @RequirePermission("workplace.view")
    @Operation(summary = "Báo cáo tỷ lệ lấp đầy không gian văn phòng")
    public ResponseEntity<ApiResponse<WorkplaceOccupancyReportResponse>> getOccupancyReport(@RequestParam(required = false) UUID siteId) {
        WorkplaceOccupancyReportResponse response = searchService.generateOccupancyReport(siteId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

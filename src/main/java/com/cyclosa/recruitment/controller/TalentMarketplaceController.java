package com.cyclosa.recruitment.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.service.TalentMarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Talent Marketplace", description = "Thị trường nhân tài nội bộ: cơ hội dự án, vị trí mở nội bộ và biệt phái")
@RestController
@RequiredArgsConstructor
public class TalentMarketplaceController {

    private final TalentMarketplaceService marketplaceService;
    private final EmployeeService employeeService;

    @Operation(summary = "Danh sách cơ hội nội bộ (Dự án / Vị trí)")
    @GetMapping("/api/v1/internal-opportunities")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    public ResponseEntity<ApiResponse<PageData<InternalOpportunityResponse>>> getOpportunities(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @ModelAttribute OpportunityFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getOpportunities(companyId, filter, pageable),
                "Lấy danh sách cơ hội nội bộ thành công"));
    }

    @Operation(summary = "Chi tiết cơ hội nội bộ")
    @GetMapping("/api/v1/internal-opportunities/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    public ResponseEntity<ApiResponse<InternalOpportunityResponse>> getOpportunityById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getOpportunityById(companyId, id),
                "Lấy thông tin cơ hội thành công"));
    }

    @Operation(summary = "Đăng cơ hội nội bộ mới (Dự án ngắn hạn / Vị trí mở)")
    @PostMapping("/api/v1/internal-opportunities")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    public ResponseEntity<ApiResponse<InternalOpportunityResponse>> createOpportunity(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateOpportunityRequest request) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                marketplaceService.createOpportunity(companyId, request),
                "Đăng cơ hội nội bộ thành công"));
    }

    @Operation(summary = "Cập nhật cơ hội nội bộ")
    @PutMapping("/api/v1/internal-opportunities/{id}")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    public ResponseEntity<ApiResponse<InternalOpportunityResponse>> updateOpportunity(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOpportunityRequest request) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.updateOpportunity(companyId, id, request),
                "Cập nhật cơ hội nội bộ thành công"));
    }

    @Operation(summary = "Đóng cơ hội nội bộ")
    @PostMapping("/api/v1/internal-opportunities/{id}/close")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    public ResponseEntity<ApiResponse<Void>> closeOpportunity(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        marketplaceService.closeOpportunity(companyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Đóng cơ hội nội bộ thành công"));
    }

    @Operation(summary = "Nhân viên tự ứng tuyển / bày tỏ quan tâm vào cơ hội nội bộ (Express Interest)")
    @PostMapping("/api/v1/internal-opportunities/{id}/express-interest")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    public ResponseEntity<ApiResponse<InternalApplicationResponse>> expressInterest(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @RequestBody(required = false) ExpressInterestRequest request) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        UUID employeeId = resolveEmployeeIdFromUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                marketplaceService.expressInterest(companyId, id, employeeId, request),
                "Bày tỏ quan tâm thành công"));
    }

    @Operation(summary = "Quản lý xem danh sách ứng viên bày tỏ quan tâm cho một cơ hội")
    @GetMapping("/api/v1/internal-opportunities/{id}/applications")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    public ResponseEntity<ApiResponse<List<InternalApplicationResponse>>> getApplications(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getApplicationsByOpportunity(companyId, id),
                "Lấy danh sách ứng viên thành công"));
    }

    @Operation(summary = "Nhân viên xem danh sách các cơ hội mình đã ứng tuyển")
    @GetMapping("/api/v1/internal-marketplace/my-applications")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    public ResponseEntity<ApiResponse<List<InternalApplicationResponse>>> getMyApplications(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        UUID employeeId = resolveEmployeeIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getMyApplications(companyId, employeeId),
                "Lấy danh sách cơ hội đã ứng tuyển thành công"));
    }

    @Operation(summary = "Phê duyệt / Từ chối đơn bày tỏ quan tâm nội bộ")
    @PutMapping("/api/v1/internal-applications/{id}/review")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    public ResponseEntity<ApiResponse<InternalApplicationResponse>> reviewApplication(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewInternalApplicationRequest request) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.reviewApplication(companyId, id, request),
                "Cập nhật trạng thái ứng tuyển thành công"));
    }

    @Operation(summary = "Gợi ý cơ hội nội bộ phù hợp cho nhân viên")
    @GetMapping("/api/v1/internal-marketplace/recommendations")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    public ResponseEntity<ApiResponse<List<InternalOpportunityResponse>>> getRecommendations(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        UUID employeeId = resolveEmployeeIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getRecommendedOpportunities(companyId, employeeId),
                "Lấy gợi ý cơ hội thành công"));
    }

    @Operation(summary = "Tạo phân công nhiệm vụ biệt phái / dự án nội bộ (Assignment)")
    @PostMapping("/api/v1/internal-assignments")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    public ResponseEntity<ApiResponse<InternalAssignmentResponse>> createAssignment(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateAssignmentRequest request) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                marketplaceService.createAssignment(companyId, request),
                "Tạo nhiệm vụ phân công thành công"));
    }

    @Operation(summary = "Danh sách nhiệm vụ phân công nội bộ")
    @GetMapping("/api/v1/internal-assignments")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    public ResponseEntity<ApiResponse<List<InternalAssignmentResponse>>> getAssignments(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID opportunityId) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getAssignments(companyId, employeeId, opportunityId),
                "Lấy danh sách nhiệm vụ thành công"));
    }

    @Operation(summary = "Đánh giá và hoàn thành nhiệm vụ phân công nội bộ")
    @PutMapping("/api/v1/internal-assignments/{id}/complete")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    public ResponseEntity<ApiResponse<InternalAssignmentResponse>> completeAssignment(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteAssignmentRequest request) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.completeAssignment(companyId, id, request),
                "Hoàn thành nhiệm vụ phân công thành công"));
    }

    @Operation(summary = "Báo cáo thống kê thị trường nhân tài nội bộ")
    @GetMapping("/api/v1/internal-marketplace/stats")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    public ResponseEntity<ApiResponse<MarketplaceStatsResponse>> getStats(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId) {
        UUID companyId = headerCompanyId != null ? headerCompanyId : resolveCompanyIdFromUser();
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getStats(companyId),
                "Lấy thống kê thành công"));
    }

    private UUID resolveCompanyIdFromUser() {
        try {
            UUID userId = SecurityUtils.getCurrentUserId();
            UUID empId = employeeService.findEmployeeIdByUserId(userId).orElse(null);
            if (empId != null) {
                EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(empId);
                if (emp != null && emp.getCompany() != null) {
                    return emp.getCompany().getId();
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private UUID resolveEmployeeIdFromUser() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return employeeService.findEmployeeIdByUserId(userId)
                .orElseThrow(AppException::unauthorized);
    }
}

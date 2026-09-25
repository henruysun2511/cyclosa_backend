package com.cyclosa.recruitment.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.enums.JobPositionStatus;
import com.cyclosa.recruitment.enums.PostingStatus;
import com.cyclosa.recruitment.service.RecruitmentService;
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

import java.util.UUID;

@Tag(name = "Recruitment", description = "Quản lý tuyển dụng nhân sự: đề xuất, vị trí, tin đăng, ứng viên, ATS pipeline, offer và tiếp nhận nhân sự")
@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    // =========================================================================
    // 1. MANPOWER REQUESTS
    // =========================================================================

    @GetMapping("/api/v1/manpower-requests")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách đề xuất tuyển dụng nhân sự")
    public ResponseEntity<ApiResponse<PageData<ManpowerRequestResponse>>> getManpowerRequests(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute ManpowerRequestFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getManpowerRequests(headerCompanyId, filter, pageable),
                "Lấy danh sách đề xuất tuyển dụng thành công"));
    }

    @GetMapping("/api/v1/manpower-requests/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết đề xuất tuyển dụng")
    public ResponseEntity<ApiResponse<ManpowerRequestResponse>> getManpowerRequestById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getManpowerRequestById(headerCompanyId, id),
                "Lấy thông tin đề xuất thành công"));
    }

    @PostMapping("/api/v1/manpower-requests")
    @PreAuthorize("@perm.has('recruitment.request')")
    @RequirePermission("recruitment.request")
    @Operation(summary = "Tạo đề xuất tuyển dụng mới (tự động trình duyệt Workflow)")
    public ResponseEntity<ApiResponse<ManpowerRequestResponse>> createManpowerRequest(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateManpowerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.createManpowerRequest(headerCompanyId, request),
                "Tạo đề xuất tuyển dụng thành công"));
    }

    @PutMapping("/api/v1/manpower-requests/{id}/cancel")
    @PreAuthorize("@perm.has('recruitment.request')")
    @RequirePermission("recruitment.request")
    @Operation(summary = "Hủy đề xuất tuyển dụng đang chờ duyệt")
    public ResponseEntity<ApiResponse<Void>> cancelManpowerRequest(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        recruitmentService.cancelManpowerRequest(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Hủy đề xuất tuyển dụng thành công"));
    }

    // =========================================================================
    // 2. JOB POSITIONS
    // =========================================================================

    @GetMapping("/api/v1/job-positions")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách vị trí tuyển dụng")
    public ResponseEntity<ApiResponse<PageData<JobPositionResponse>>> getJobPositions(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute JobPositionFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getJobPositions(headerCompanyId, filter, pageable),
                "Lấy danh sách vị trí tuyển dụng thành công"));
    }

    @GetMapping("/api/v1/job-positions/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết vị trí tuyển dụng")
    public ResponseEntity<ApiResponse<JobPositionResponse>> getJobPositionById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getJobPositionById(headerCompanyId, id),
                "Lấy thông tin vị trí tuyển dụng thành công"));
    }

    @PostMapping("/api/v1/job-positions")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Tạo vị trí tuyển dụng mới từ đề xuất đã duyệt")
    public ResponseEntity<ApiResponse<JobPositionResponse>> createJobPosition(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateJobPositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.createJobPosition(headerCompanyId, request),
                "Tạo vị trí tuyển dụng thành công"));
    }

    @PutMapping("/api/v1/job-positions/{id}/status")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Cập nhật trạng thái vị trí tuyển dụng (OPEN, PAUSED, CLOSED)")
    public ResponseEntity<ApiResponse<JobPositionResponse>> updateJobPositionStatus(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @RequestParam JobPositionStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.updateJobPositionStatus(headerCompanyId, id, status),
                "Cập nhật trạng thái vị trí tuyển dụng thành công"));
    }

    // =========================================================================
    // 3. JOB POSTINGS
    // =========================================================================

    @GetMapping("/api/v1/job-postings")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách tin đăng tuyển đa kênh")
    public ResponseEntity<ApiResponse<PageData<JobPostingResponse>>> getJobPostings(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute JobPostingFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getJobPostings(headerCompanyId, filter, pageable),
                "Lấy danh sách tin tuyển dụng thành công"));
    }

    @GetMapping("/api/v1/job-postings/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết tin tuyển dụng")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getJobPostingById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getJobPostingById(headerCompanyId, id),
                "Lấy chi tiết tin tuyển dụng thành công"));
    }

    @PostMapping("/api/v1/job-postings")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Tạo mới tin đăng tuyển dụng")
    public ResponseEntity<ApiResponse<JobPostingResponse>> createJobPosting(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateJobPostingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.createJobPosting(headerCompanyId, request),
                "Tạo tin đăng tuyển thành công"));
    }

    @PutMapping("/api/v1/job-postings/{id}/status")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Cập nhật trạng thái tin tuyển dụng (DRAFT, PUBLISHED, EXPIRED, CLOSED)")
    public ResponseEntity<ApiResponse<JobPostingResponse>> updateJobPostingStatus(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @RequestParam PostingStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.updateJobPostingStatus(headerCompanyId, id, status),
                "Cập nhật trạng thái tin tuyển dụng thành công"));
    }

    // =========================================================================
    // 4. CANDIDATES
    // =========================================================================

    @GetMapping("/api/v1/candidates")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách ứng viên")
    public ResponseEntity<ApiResponse<PageData<CandidateResponse>>> getCandidates(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute CandidateFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getCandidates(headerCompanyId, filter, pageable),
                "Lấy danh sách ứng viên thành công"));
    }

    @GetMapping("/api/v1/candidates/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết hồ sơ ứng viên")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getCandidateById(headerCompanyId, id),
                "Lấy thông tin ứng viên thành công"));
    }

    @PostMapping("/api/v1/candidates")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Tiếp nhận và tạo mới hồ sơ ứng viên")
    public ResponseEntity<ApiResponse<CandidateResponse>> createCandidate(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateCandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.createCandidate(headerCompanyId, request),
                "Tạo hồ sơ ứng viên thành công"));
    }

    @PutMapping("/api/v1/candidates/{id}")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Cập nhật thông tin hồ sơ ứng viên")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateCandidate(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCandidateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.updateCandidate(headerCompanyId, id, request),
                "Cập nhật hồ sơ ứng viên thành công"));
    }

    // =========================================================================
    // 5. APPLICATIONS (ATS PIPELINE)
    // =========================================================================

    @GetMapping("/api/v1/applications")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách đơn ứng tuyển (ATS Pipeline)")
    public ResponseEntity<ApiResponse<PageData<ApplicationResponse>>> getApplications(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute ApplicationFilter filter,
            @PageableDefault(sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getApplications(headerCompanyId, filter, pageable),
                "Lấy danh sách đơn ứng tuyển thành công"));
    }

    @GetMapping("/api/v1/applications/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết đơn ứng tuyển")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getApplicationById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getApplicationById(headerCompanyId, id),
                "Lấy thông tin đơn ứng tuyển thành công"));
    }

    @PostMapping("/api/v1/applications")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Nộp đơn ứng tuyển vào vị trí công việc")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> submitApplication(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody SubmitApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.submitApplication(headerCompanyId, request),
                "Nộp đơn ứng tuyển thành công"));
    }

    @PutMapping("/api/v1/applications/{id}/stage")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Chuyển giai đoạn ứng tuyển (Screening -> Interview -> Offer -> Hired)")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> updateApplicationStage(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationStageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.updateApplicationStage(headerCompanyId, id, request),
                "Chuyển giai đoạn ứng tuyển thành công"));
    }

    // =========================================================================
    // 6. OFFERS & HIRING CONVERT-TO-EMPLOYEE
    // =========================================================================

    @GetMapping("/api/v1/offers")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách thư mời nhận việc (Offers)")
    public ResponseEntity<ApiResponse<PageData<OfferResponse>>> getOffers(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute OfferFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getOffers(headerCompanyId, filter, pageable),
                "Lấy danh sách thư mời nhận việc thành công"));
    }

    @GetMapping("/api/v1/offers/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết thư mời nhận việc")
    public ResponseEntity<ApiResponse<OfferResponse>> getOfferById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getOfferById(headerCompanyId, id),
                "Lấy chi tiết thư mời thành công"));
    }

    @PostMapping("/api/v1/offers")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Lập thư mời nhận việc (tự động trình duyệt Workflow)")
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.createOffer(headerCompanyId, request),
                "Lập thư mời nhận việc thành công"));
    }

    @PutMapping("/api/v1/offers/{id}/status")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Cập nhật phản hồi Offer (ACCEPTED, DECLINED)")
    public ResponseEntity<ApiResponse<OfferResponse>> updateOfferStatus(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody RespondOfferRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.updateOfferStatus(headerCompanyId, id, request),
                "Cập nhật phản hồi Offer thành công"));
    }

    @PostMapping("/api/v1/hiring/{application_id}/convert-to-employee")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Tiếp nhận nhân sự: chuyển đổi ứng viên trúng tuyển thành nhân viên chính thức")
    public ResponseEntity<ApiResponse<ConvertToEmployeeResponse>> convertToEmployee(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable("application_id") UUID applicationId,
            @Valid @RequestBody ConvertToEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.convertToEmployee(headerCompanyId, applicationId, request),
                "Tiếp nhận ứng viên thành nhân viên chính thức thành công"));
    }

    // =========================================================================
    // 7. TALENT POOL
    // =========================================================================

    @GetMapping("/api/v1/talent-pool")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Tra cứu ngân hàng hồ sơ ứng viên tiềm năng (Talent Pool)")
    public ResponseEntity<ApiResponse<PageData<TalentPoolResponse>>> getTalentPool(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @ModelAttribute TalentPoolFilter filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                recruitmentService.getTalentPool(headerCompanyId, filter, pageable),
                "Lấy danh sách Talent Pool thành công"));
    }

    @PostMapping("/api/v1/talent-pool")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Lưu ứng viên vào Talent Pool")
    public ResponseEntity<ApiResponse<TalentPoolResponse>> addToTalentPool(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody AddToTalentPoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                recruitmentService.addToTalentPool(headerCompanyId, request),
                "Lưu ứng viên vào Talent Pool thành công"));
    }

    @DeleteMapping("/api/v1/talent-pool/{id}")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Xóa ứng viên khỏi Talent Pool")
    public ResponseEntity<ApiResponse<Void>> removeFromTalentPool(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        recruitmentService.removeFromTalentPool(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa ứng viên khỏi Talent Pool thành công"));
    }
}

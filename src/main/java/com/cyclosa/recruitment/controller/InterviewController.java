package com.cyclosa.recruitment.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.service.InterviewService;
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

@Tag(name = "Interviews", description = "Quản lý phỏng vấn hội đồng, bộ tiêu chí Interview Kit, chấm điểm độc lập Blind Grading và phân tích thiên kiến")
@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    // =========================================================================
    // 1. INTERVIEW KITS & QUESTIONS
    // =========================================================================

    @GetMapping("/api/v1/interview-kits")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách bộ câu hỏi phỏng vấn chuẩn hóa (Interview Kits)")
    public ResponseEntity<ApiResponse<PageData<InterviewKitResponse>>> getInterviewKits(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @RequestParam(required = false) UUID jobPositionId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.getInterviewKits(headerCompanyId, jobPositionId, pageable),
                "Lấy danh sách bộ phỏng vấn thành công"));
    }

    @GetMapping("/api/v1/interview-kits/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết bộ phỏng vấn kèm danh sách tiêu chí Rubric")
    public ResponseEntity<ApiResponse<InterviewKitResponse>> getInterviewKitById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.getInterviewKitById(headerCompanyId, id),
                "Lấy chi tiết bộ phỏng vấn thành công"));
    }

    @PostMapping("/api/v1/interview-kits")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Tạo mới bộ phỏng vấn và tiêu chí chấm điểm")
    public ResponseEntity<ApiResponse<InterviewKitResponse>> createInterviewKit(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateInterviewKitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                interviewService.createInterviewKit(headerCompanyId, request),
                "Tạo bộ phỏng vấn thành công"));
    }

    @PutMapping("/api/v1/interview-kits/{id}")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Cập nhật bộ phỏng vấn")
    public ResponseEntity<ApiResponse<InterviewKitResponse>> updateInterviewKit(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInterviewKitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.updateInterviewKit(headerCompanyId, id, request),
                "Cập nhật bộ phỏng vấn thành công"));
    }

    @DeleteMapping("/api/v1/interview-kits/{id}")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Xóa bộ phỏng vấn")
    public ResponseEntity<ApiResponse<Void>> deleteInterviewKit(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        interviewService.deleteInterviewKit(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa bộ phỏng vấn thành công"));
    }

    @PostMapping("/api/v1/interview-kits/{id}/questions")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Thêm câu hỏi & tiêu chuẩn thang điểm vào Kit")
    public ResponseEntity<ApiResponse<InterviewQuestionResponse>> addQuestionToKit(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateInterviewQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                interviewService.addQuestionToKit(headerCompanyId, id, request),
                "Thêm câu hỏi vào bộ phỏng vấn thành công"));
    }

    @DeleteMapping("/api/v1/interview-kits/questions/{questionId}")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Xóa câu hỏi khỏi bộ phỏng vấn")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID questionId) {
        interviewService.deleteQuestion(headerCompanyId, questionId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa câu hỏi thành công"));
    }

    // =========================================================================
    // 2. INTERVIEWS & EVALUATIONS
    // =========================================================================

    @GetMapping("/api/v1/interviews")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Danh sách lịch phỏng vấn theo hồ sơ ứng tuyển")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getInterviews(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @RequestParam UUID applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.getInterviewsByApplication(headerCompanyId, applicationId),
                "Lấy danh sách lịch phỏng vấn thành công"));
    }

    @GetMapping("/api/v1/interviews/{id}")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Chi tiết buổi phỏng vấn")
    public ResponseEntity<ApiResponse<InterviewResponse>> getInterviewById(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.getInterviewById(headerCompanyId, id),
                "Lấy chi tiết buổi phỏng vấn thành công"));
    }

    @PostMapping("/api/v1/interviews")
    @PreAuthorize("@perm.has('recruitment.manage')")
    @RequirePermission("recruitment.manage")
    @Operation(summary = "Lên lịch phỏng vấn và chỉ định hội đồng giám khảo")
    public ResponseEntity<ApiResponse<InterviewResponse>> scheduleInterview(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody ScheduleInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                interviewService.scheduleInterview(headerCompanyId, request),
                "Lên lịch phỏng vấn thành công"));
    }

    @PostMapping("/api/v1/interviews/{id}/evaluations")
    @PreAuthorize("@perm.has('recruitment.interview')")
    @RequirePermission("recruitment.interview")
    @Operation(summary = "Giám khảo nộp phiếu chấm điểm đánh giá (Blind Grading)")
    public ResponseEntity<ApiResponse<EvaluationResponse>> submitEvaluation(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @RequestParam UUID interviewerEmployeeId,
            @Valid @RequestBody SubmitEvaluationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                interviewService.submitEvaluation(headerCompanyId, id, interviewerEmployeeId, request),
                "Nộp phiếu đánh giá phỏng vấn thành công"));
    }

    @GetMapping("/api/v1/interviews/{id}/consolidated-feedback")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Xem tổng hợp nhận xét & cảnh báo độ lệch điểm (Divergence Alert)")
    public ResponseEntity<ApiResponse<ConsolidatedFeedbackResponse>> getConsolidatedFeedback(
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.getConsolidatedFeedback(headerCompanyId, id),
                "Lấy tổng hợp đánh giá phỏng vấn thành công"));
    }

    @GetMapping("/api/v1/interviewers/{id}/scoring-pattern")
    @PreAuthorize("@perm.has('recruitment.view')")
    @RequirePermission("recruitment.view")
    @Operation(summary = "Phân tích xu hướng chấm điểm của giám khảo (Bias Awareness: Hawkish / Dovish)")
    public ResponseEntity<ApiResponse<InterviewerScoringPatternResponse>> getInterviewerScoringPattern(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.getInterviewerScoringPattern(id),
                "Phân tích xu hướng chấm điểm thành công"));
    }
}

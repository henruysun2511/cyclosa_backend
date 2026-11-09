package com.cyclosa.workflow.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.workflow.dto.request.ApproveStepRequest;
import com.cyclosa.workflow.dto.request.CancelWorkflowRequest;
import com.cyclosa.workflow.dto.request.RejectStepRequest;
import com.cyclosa.workflow.dto.request.SubmitWorkflowRequest;
import com.cyclosa.workflow.dto.request.WorkflowInboxFilterRequest;
import com.cyclosa.workflow.dto.response.WorkflowRequestDetailResponse;
import com.cyclosa.workflow.dto.response.WorkflowRequestResponse;
import com.cyclosa.workflow.dto.response.WorkflowStatsResponse;
import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.service.WorkflowExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow-requests")
@RequiredArgsConstructor
@Tag(name = "Workflow Requests", description = "Hộp thư công việc và xử lý tiến trình phê duyệt")
public class WorkflowRequestController {

    private final WorkflowExecutionService workflowExecutionService;

    @PostMapping
    @Operation(summary = "Khởi tạo và nộp hồ sơ yêu cầu phê duyệt")
    public ResponseEntity<ApiResponse<WorkflowRequestDetailResponse>> submit(
            @Valid @RequestBody SubmitWorkflowRequest request
    ) {
        WorkflowRequestDetailResponse data = workflowExecutionService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data, "Gửi yêu cầu phê duyệt thành công"));
    }

    @GetMapping("/inbox")
    @Operation(summary = "Hộp thư việc cần tôi phê duyệt (các bước đang chờ xử lý)")
    public ResponseEntity<ApiResponse<PageData<WorkflowRequestResponse>>> getMyInbox(
            @Valid @ParameterObject WorkflowInboxFilterRequest filter
    ) {
        PageData<WorkflowRequestResponse> data = workflowExecutionService.getMyInbox(filter);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy danh sách việc cần duyệt thành công"));
    }

    @GetMapping("/my-submitted")
    @Operation(summary = "Danh sách các yêu cầu do chính tôi gửi duyệt")
    public ResponseEntity<ApiResponse<PageData<WorkflowRequestResponse>>> getMySubmitted(
            @Valid @ParameterObject WorkflowInboxFilterRequest filter
    ) {
        PageData<WorkflowRequestResponse> data = workflowExecutionService.getMySubmitted(filter);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy danh sách yêu cầu đã gửi thành công"));
    }

    @GetMapping("/history")
    @Operation(summary = "Lịch sử các hồ sơ tôi đã từng phê duyệt hoặc từ chối")
    public ResponseEntity<ApiResponse<PageData<WorkflowRequestResponse>>> getHistory(
            @Valid @ParameterObject WorkflowInboxFilterRequest filter
    ) {
        PageData<WorkflowRequestResponse> data = workflowExecutionService.getHistory(filter);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy lịch sử phê duyệt thành công"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Lấy thống kê số lượng hồ sơ phê duyệt cho thẻ KPI")
    public ResponseEntity<ApiResponse<WorkflowStatsResponse>> getStats() {
        WorkflowStatsResponse data = workflowExecutionService.getStats();
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy số liệu thống kê thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết hồ sơ phê duyệt và toàn bộ dòng thời gian các bước")
    public ResponseEntity<ApiResponse<WorkflowRequestDetailResponse>> getDetail(@PathVariable UUID id) {
        WorkflowRequestDetailResponse data = workflowExecutionService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy chi tiết yêu cầu thành công"));
    }

    @GetMapping("/by-reference")
    @Operation(summary = "Tra cứu tiến trình phê duyệt của một chứng từ cụ thể (Hợp đồng, Tuyển dụng...)")
    public ResponseEntity<ApiResponse<WorkflowRequestDetailResponse>> getByReference(
            @RequestParam RequestType requestType,
            @RequestParam UUID referenceId
    ) {
        WorkflowRequestDetailResponse data = workflowExecutionService.getByReference(requestType, referenceId);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy tiến trình phê duyệt theo chứng từ thành công"));
    }

    @PostMapping("/{id}/steps/{stepId}/approve")
    @Operation(summary = "Phê duyệt bước (chấp thuận và chuyển bước kế tiếp)")
    public ResponseEntity<ApiResponse<WorkflowRequestDetailResponse>> approveStep(
            @PathVariable UUID id,
            @PathVariable UUID stepId,
            @RequestBody(required = false) ApproveStepRequest request
    ) {
        if (request == null) {
            request = new ApproveStepRequest();
        }
        WorkflowRequestDetailResponse data = workflowExecutionService.approveStep(id, stepId, request);
        return ResponseEntity.ok(ApiResponse.ok(data, "Phê duyệt bước thành công"));
    }

    @PostMapping("/{id}/steps/{stepId}/reject")
    @Operation(summary = "Từ chối bước (kèm lý do giải trình bắt buộc)")
    public ResponseEntity<ApiResponse<WorkflowRequestDetailResponse>> rejectStep(
            @PathVariable UUID id,
            @PathVariable UUID stepId,
            @Valid @RequestBody RejectStepRequest request
    ) {
        WorkflowRequestDetailResponse data = workflowExecutionService.rejectStep(id, stepId, request);
        return ResponseEntity.ok(ApiResponse.ok(data, "Đã từ chối yêu cầu phê duyệt"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Người gửi rút lại / Hủy yêu cầu phê duyệt đang chờ")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelWorkflowRequest request
    ) {
        workflowExecutionService.cancel(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã hủy yêu cầu phê duyệt"));
    }
}


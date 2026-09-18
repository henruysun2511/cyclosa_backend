package com.cyclosa.workflow.controller;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.ApprovalActionRequest;
import com.cyclosa.workflow.dto.request.StartWorkflowRequest;
import com.cyclosa.workflow.dto.response.PendingApprovalResponse;
import com.cyclosa.workflow.dto.response.WorkflowHistoryResponse;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.service.WorkflowEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflow Instances & Approvals", description = "Vận hành phê duyệt đa cấp và hộp thư phê duyệt")
public class WorkflowInstanceController {

    private final WorkflowEngineService workflowEngineService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/instances")
    @Operation(summary = "Khởi tạo một phiên phê duyệt mới cho bản ghi nghiệp vụ")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> startWorkflow(
            @Valid @RequestBody StartWorkflowRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                workflowEngineService.startWorkflow(request),
                "Khởi tạo quy trình phê duyệt thành công"));
    }

    @PutMapping("/instances/{id}/approve")
    @Operation(summary = "Phê duyệt bước hiện tại của yêu cầu")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovalActionRequest request
    ) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(ApiResponse.ok(
                workflowEngineService.approve(id, currentEmployeeId, request),
                "Phê duyệt thành công"));
    }

    @PutMapping("/instances/{id}/reject")
    @Operation(summary = "Từ chối bước hiện tại của yêu cầu")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovalActionRequest request
    ) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(ApiResponse.ok(
                workflowEngineService.reject(id, currentEmployeeId, request),
                "Từ chối yêu cầu thành công"));
    }

    @PutMapping("/instances/{id}/cancel")
    @Operation(summary = "Hủy yêu cầu phê duyệt (bởi người tạo đơn)")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Người dùng tự hủy") String reason
    ) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(ApiResponse.ok(
                workflowEngineService.cancel(id, currentEmployeeId, reason),
                "Hủy yêu cầu thành công"));
    }

    @GetMapping("/pending")
    @Operation(summary = "Lấy danh sách yêu cầu đang chờ tôi phê duyệt (bao gồm cả ủy quyền)")
    public ResponseEntity<ApiResponse<PageData<PendingApprovalResponse>>> getPendingApprovals(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(ApiResponse.ok(
                workflowEngineService.getPendingApprovals(currentEmployeeId, pageable),
                "Lấy danh sách chờ duyệt thành công"));
    }

    @GetMapping("/instances/{id}/history")
    @Operation(summary = "Xem lịch sử và tiến trình timeline chi tiết của quy trình phê duyệt")
    public ResponseEntity<ApiResponse<WorkflowHistoryResponse>> getInstanceHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                workflowEngineService.getInstanceHistory(id),
                "Lấy lịch sử phê duyệt thành công"));
    }

    @GetMapping("/instances/by-request")
    @Operation(summary = "Tra cứu phiên phê duyệt theo loại yêu cầu và ID nghiệp vụ gốc")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> getInstanceByRequest(
            @RequestParam ApprovalRequestType requestType,
            @RequestParam UUID requestId
    ) {
        return workflowEngineService.getInstanceByRequest(requestType, requestId)
                .map(res -> ResponseEntity.ok(ApiResponse.ok(res, "Tìm thấy phiên phê duyệt")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Không tìm thấy phiên phê duyệt cho yêu cầu này")));
    }

    private UUID getCurrentEmployeeIdOrThrow() {
        return SecurityUtils.getCurrentUserIdOptional()
                .flatMap(employeeRepository::findByUserId)
                .map(Employee::getId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND,
                        "Không tìm thấy hồ sơ nhân viên ứng với tài khoản đăng nhập hiện tại"));
    }
}

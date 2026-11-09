package com.cyclosa.workflow.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.workflow.dto.request.CreateDelegationRequest;
import com.cyclosa.workflow.dto.response.WorkflowDelegationResponse;
import com.cyclosa.workflow.service.WorkflowDelegationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow-delegations")
@RequiredArgsConstructor
@Tag(name = "Workflow Delegations", description = "Quản lý ủy quyền thẩm quyền phê duyệt")
public class WorkflowDelegationController {

    private final WorkflowDelegationService workflowDelegationService;

    @PostMapping
    @Operation(summary = "Tạo mới lượt ủy quyền phê duyệt cho nhân sự khác")
    public ResponseEntity<ApiResponse<WorkflowDelegationResponse>> create(
            @Valid @RequestBody CreateDelegationRequest request
    ) {
        WorkflowDelegationResponse data = workflowDelegationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data, "Thiết lập ủy quyền phê duyệt thành công"));
    }

    @GetMapping("/my")
    @Operation(summary = "Danh sách các lượt ủy quyền do tôi thiết lập")
    public ResponseEntity<ApiResponse<PageData<WorkflowDelegationResponse>>> getMyDelegations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageData<WorkflowDelegationResponse> data = workflowDelegationService.getMyDelegations(page, size);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy danh sách ủy quyền thành công"));
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Thu hồi hiệu lực lượt ủy quyền trước hạn")
    public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable UUID id) {
        workflowDelegationService.revoke(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã thu hồi lượt ủy quyền thành công"));
    }
}


package com.cyclosa.workflow.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workflow.dto.request.*;
import com.cyclosa.workflow.dto.response.WorkflowConditionResponse;
import com.cyclosa.workflow.dto.response.WorkflowDefinitionResponse;
import com.cyclosa.workflow.dto.response.WorkflowStepResponse;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.service.WorkflowDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow-definitions")
@RequiredArgsConstructor
@Tag(name = "Workflow Definitions", description = "Quản trị định nghĩa quy trình phê duyệt đa cấp động")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService definitionService;
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Tạo bản nháp quy trình phê duyệt mới")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> createWorkflowDefinition(
            @Valid @RequestBody CreateWorkflowDefinitionRequest request
    ) {
        UUID creatorEmployeeId = getCurrentEmployeeId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                definitionService.createWorkflowDefinition(request, creatorEmployeeId),
                "Tạo quy trình phê duyệt thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Cập nhật thông tin bản nháp quy trình phê duyệt")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> updateWorkflowDefinition(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkflowDefinitionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                definitionService.updateWorkflowDefinition(id, request),
                "Cập nhật quy trình thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('workflow.view')")
    @RequirePermission("workflow.view")
    @Operation(summary = "Xem chi tiết quy trình phê duyệt theo ID kèm các bước và điều kiện")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> getWorkflowDefinitionById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                definitionService.getWorkflowDefinitionById(id),
                "Lấy thông tin quy trình thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('workflow.view')")
    @RequirePermission("workflow.view")
    @Operation(summary = "Danh sách phân trang các quy trình phê duyệt theo công ty")
    public ResponseEntity<ApiResponse<PageData<WorkflowDefinitionResponse>>> getWorkflowDefinitions(
            @RequestParam UUID companyId,
            @RequestParam(required = false) ApprovalRequestType requestType,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                definitionService.getWorkflowDefinitions(companyId, requestType, pageable),
                "Lấy danh sách quy trình thành công"));
    }

    @PostMapping("/{id}/steps")
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Thêm bước phê duyệt vào bản nháp quy trình")
    public ResponseEntity<ApiResponse<WorkflowStepResponse>> addStep(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWorkflowStepRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                definitionService.addStep(id, request),
                "Thêm bước phê duyệt thành công"));
    }

    @DeleteMapping("/{id}/steps/{stepId}")
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Xóa bước phê duyệt khỏi bản nháp quy trình")
    public ResponseEntity<ApiResponse<Void>> deleteStep(
            @PathVariable UUID id,
            @PathVariable UUID stepId
    ) {
        definitionService.deleteStep(id, stepId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa bước phê duyệt thành công"));
    }

    @PostMapping("/{id}/conditions")
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Thêm điều kiện rẽ nhánh vào bản nháp quy trình")
    public ResponseEntity<ApiResponse<WorkflowConditionResponse>> addCondition(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWorkflowConditionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                definitionService.addCondition(id, request),
                "Thêm điều kiện rẽ nhánh thành công"));
    }

    @DeleteMapping("/{id}/conditions/{conditionId}")
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Xóa điều kiện rẽ nhánh khỏi bản nháp quy trình")
    public ResponseEntity<ApiResponse<Void>> deleteCondition(
            @PathVariable UUID id,
            @PathVariable UUID conditionId
    ) {
        definitionService.deleteCondition(id, conditionId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa điều kiện rẽ nhánh thành công"));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Xuất bản quy trình phê duyệt để kích hoạt trong toàn hệ thống")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> publishWorkflowDefinition(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                definitionService.publish(id),
                "Xuất bản quy trình phê duyệt thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('workflow.manage')")
    @RequirePermission("workflow.manage")
    @Operation(summary = "Xóa bản nháp quy trình phê duyệt")
    public ResponseEntity<ApiResponse<Void>> deleteWorkflowDefinition(@PathVariable UUID id) {
        definitionService.deleteWorkflowDefinition(id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa quy trình thành công"));
    }

    private UUID getCurrentEmployeeId() {
        return SecurityUtils.getCurrentUserIdOptional()
                .flatMap(employeeService::findEmployeeIdByUserId)
                .orElse(null);
    }
}

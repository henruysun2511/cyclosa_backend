package com.cyclosa.workflow.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.cyclosa.workflow.dto.request.UpdateWorkflowDefinitionRequest;
import com.cyclosa.workflow.dto.request.WorkflowDefinitionFilterRequest;
import com.cyclosa.workflow.dto.response.WorkflowDefinitionDetailResponse;
import com.cyclosa.workflow.dto.response.WorkflowDefinitionResponse;
import com.cyclosa.workflow.service.WorkflowDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow-definitions")
@RequiredArgsConstructor
@Tag(name = "Workflow Definitions", description = "Quản lý định nghĩa và cấu hình quy trình phê duyệt")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService workflowDefinitionService;

    @GetMapping
    @PreAuthorize("hasAuthority('workflow.manage') or hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách quy trình phê duyệt có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<PageData<WorkflowDefinitionResponse>>> search(
            @Valid @ParameterObject WorkflowDefinitionFilterRequest filter
    ) {
        PageData<WorkflowDefinitionResponse> data = workflowDefinitionService.search(filter);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy danh sách quy trình thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('workflow.manage') or hasRole('ADMIN')")
    @Operation(summary = "Tạo mới định nghĩa quy trình phê duyệt kèm danh sách các bước")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDetailResponse>> create(
            @Valid @RequestBody CreateWorkflowDefinitionRequest request
    ) {
        WorkflowDefinitionDetailResponse data = workflowDefinitionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data, "Tạo quy trình phê duyệt thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow.manage') or hasRole('ADMIN')")
    @Operation(summary = "Xem chi tiết quy trình phê duyệt và các bước cấu hình")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDetailResponse>> getDetail(@PathVariable UUID id) {
        WorkflowDefinitionDetailResponse data = workflowDefinitionService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy chi tiết quy trình thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow.manage') or hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin và danh sách các bước của quy trình")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDetailResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkflowDefinitionRequest request
    ) {
        WorkflowDefinitionDetailResponse data = workflowDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(data, "Cập nhật quy trình phê duyệt thành công"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('workflow.manage') or hasRole('ADMIN')")
    @Operation(summary = "Kích hoạt áp dụng quy trình phê duyệt")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        workflowDefinitionService.activate(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Kích hoạt quy trình phê duyệt thành công"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('workflow.manage') or hasRole('ADMIN')")
    @Operation(summary = "Ngừng kích hoạt quy trình phê duyệt")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        workflowDefinitionService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã ngừng kích hoạt quy trình phê duyệt"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow.manage') or hasRole('ADMIN')")
    @Operation(summary = "Xóa định nghĩa quy trình phê duyệt")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        workflowDefinitionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã xóa quy trình phê duyệt"));
    }
}


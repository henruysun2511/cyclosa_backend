package com.cyclosa.workflow.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workflow.dto.request.CreateWorkflowDelegateRequest;
import com.cyclosa.workflow.dto.response.WorkflowDelegateResponse;
import com.cyclosa.workflow.service.WorkflowDelegateService;
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
@RequestMapping("/api/v1/workflows/delegates")
@RequiredArgsConstructor
@Tag(name = "Workflows", description = "Quản lý ủy quyền quyền phê duyệt khi vắng mặt")
public class WorkflowDelegateController {

    private final WorkflowDelegateService delegateService;
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("@perm.has('workflow.delegate')")
    @RequirePermission("workflow.delegate")
    @Operation(summary = "Thiết lập ủy quyền phê duyệt cho nhân viên khác")
    public ResponseEntity<ApiResponse<WorkflowDelegateResponse>> createDelegate(
            @Valid @RequestBody CreateWorkflowDelegateRequest request
    ) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                delegateService.createDelegate(request, currentEmployeeId),
                "Thiết lập ủy quyền thành công"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("@perm.has('workflow.delegate')")
    @RequirePermission("workflow.delegate")
    @Operation(summary = "Hủy bỏ một ủy quyền đang có hiệu lực")
    public ResponseEntity<ApiResponse<Void>> cancelDelegate(@PathVariable UUID id) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        delegateService.cancelDelegate(id, currentEmployeeId);
        return ResponseEntity.ok(ApiResponse.noContent("Hủy ủy quyền thành công"));
    }

    @GetMapping("/my-delegations")
    @PreAuthorize("@perm.has('workflow.view')")
    @RequirePermission("workflow.view")
    @Operation(summary = "Lấy danh sách các ủy quyền do tôi thiết lập")
    public ResponseEntity<ApiResponse<PageData<WorkflowDelegateResponse>>> getMyDelegations(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(ApiResponse.ok(
                delegateService.getMyDelegations(currentEmployeeId, pageable),
                "Lấy danh sách ủy quyền thành công"));
    }

    @GetMapping("/delegated-to-me")
    @PreAuthorize("@perm.has('workflow.view')")
    @RequirePermission("workflow.view")
    @Operation(summary = "Lấy danh sách các ủy quyền được giao cho tôi")
    public ResponseEntity<ApiResponse<PageData<WorkflowDelegateResponse>>> getDelegationsAssignedToMe(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID currentEmployeeId = getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(ApiResponse.ok(
                delegateService.getDelegationsAssignedToMe(currentEmployeeId, pageable),
                "Lấy danh sách ủy quyền được giao thành công"));
    }

    private UUID getCurrentEmployeeIdOrThrow() {
        return SecurityUtils.getCurrentUserIdOptional()
                .flatMap(employeeService::findEmployeeIdByUserId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND,
                        "Không tìm thấy hồ sơ nhân viên ứng với tài khoản đăng nhập hiện tại"));
    }
}

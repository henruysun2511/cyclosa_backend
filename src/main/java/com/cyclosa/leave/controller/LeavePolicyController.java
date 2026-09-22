package com.cyclosa.leave.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.leave.dto.request.CreateLeavePolicyRequest;
import com.cyclosa.leave.dto.request.LeavePolicyFilter;
import com.cyclosa.leave.dto.request.UpdateLeavePolicyRequest;
import com.cyclosa.leave.dto.response.LeavePolicyResponse;
import com.cyclosa.leave.service.LeavePolicyService;
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
@RequestMapping("/api/v1/leave-policies")
@RequiredArgsConstructor
@Tag(name = "Leave Policies", description = "Cấu hình chính sách định mức nghỉ phép và thâm niên theo Điều 113, 114 BLLĐ")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @GetMapping
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Lấy danh sách chính sách nghỉ phép toàn công ty")
    public ResponseEntity<ApiResponse<PageData<LeavePolicyResponse>>> getPolicies(
            @ModelAttribute LeavePolicyFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leavePolicyService.getPolicies(headerCompanyId, filter, pageable),
                "Lấy danh sách chính sách nghỉ phép thành công"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('leave.view')")
    @RequirePermission("leave.view")
    @Operation(summary = "Lấy chi tiết một chính sách nghỉ phép")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> getPolicyById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leavePolicyService.getPolicyById(headerCompanyId, id),
                "Lấy chi tiết chính sách nghỉ phép thành công"
        ));
    }

    @PostMapping
    @PreAuthorize("@perm.has('leave.manage')")
    @RequirePermission("leave.manage")
    @Operation(summary = "Tạo mới chính sách nghỉ phép")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> createPolicy(
            @Valid @RequestBody CreateLeavePolicyRequest request,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        leavePolicyService.createPolicy(headerCompanyId, request),
                        "Tạo chính sách nghỉ phép thành công"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('leave.manage')")
    @RequirePermission("leave.manage")
    @Operation(summary = "Cập nhật chính sách nghỉ phép")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLeavePolicyRequest request,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                leavePolicyService.updatePolicy(headerCompanyId, id, request),
                "Cập nhật chính sách nghỉ phép thành công"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('leave.manage')")
    @RequirePermission("leave.manage")
    @Operation(summary = "Xóa chính sách nghỉ phép")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        leavePolicyService.deletePolicy(headerCompanyId, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa chính sách nghỉ phép thành công"));
    }
}

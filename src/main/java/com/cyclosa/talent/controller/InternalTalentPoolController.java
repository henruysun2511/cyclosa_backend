package com.cyclosa.talent.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.talent.dto.filter.TalentPoolFilter;
import com.cyclosa.talent.dto.request.AddTalentPoolRequest;
import com.cyclosa.talent.dto.request.UpdateTalentPoolRequest;
import com.cyclosa.talent.dto.response.TalentPoolResponse;
import com.cyclosa.talent.service.InternalTalentPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal-talent-pool")
@RequiredArgsConstructor
@Tag(name = "Internal Talent Pool", description = "Quản lý kho nhân tài nội bộ (HiPo, Leadership Potential, Key Talent)")
public class InternalTalentPoolController {

    private final InternalTalentPoolService talentPoolService;
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("@perm.has('talent_pool.manage')")
    @RequirePermission("talent_pool.manage")
    @Operation(summary = "Đưa nhân sự vào kho nhân tài nội bộ")
    public ResponseEntity<ApiResponse<TalentPoolResponse>> addTalent(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody AddTalentPoolRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID addedByEmpId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        TalentPoolResponse response = talentPoolService.addTalent(effectiveCompanyId, request, addedByEmpId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Đưa nhân sự vào kho nhân tài thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('talent_pool.view')")
    @RequirePermission("talent_pool.view")
    @Operation(summary = "Lấy danh sách nhân sự trong kho nhân tài (có lọc theo tag và phân trang)")
    public ResponseEntity<ApiResponse<PageData<TalentPoolResponse>>> getTalentPool(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @ModelAttribute TalentPoolFilter filter
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                talentPoolService.getTalentPool(effectiveCompanyId, filter),
                "Lấy danh sách kho nhân tài thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('talent_pool.view')")
    @RequirePermission("talent_pool.view")
    @Operation(summary = "Xem chi tiết hồ sơ nhân sự trong kho nhân tài")
    public ResponseEntity<ApiResponse<TalentPoolResponse>> getTalentById(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                talentPoolService.getTalentById(effectiveCompanyId, id),
                "Lấy thông tin nhân tài thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('talent_pool.manage')")
    @RequirePermission("talent_pool.manage")
    @Operation(summary = "Cập nhật thẻ tag hoặc ghi chú đánh giá của nhân sự trong kho")
    public ResponseEntity<ApiResponse<TalentPoolResponse>> updateTalent(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTalentPoolRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                talentPoolService.updateTalent(effectiveCompanyId, id, request),
                "Cập nhật hồ sơ nhân tài thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('talent_pool.manage')")
    @RequirePermission("talent_pool.manage")
    @Operation(summary = "Xóa nhân sự khỏi kho nhân tài")
    public ResponseEntity<ApiResponse<Void>> deleteTalent(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        talentPoolService.deleteTalent(effectiveCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa nhân sự khỏi kho nhân tài thành công"));
    }
}

package com.cyclosa.contract.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.contract.dto.request.*;
import com.cyclosa.contract.dto.response.*;
import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.enums.TerminationGround;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.contract.service.ContractTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Quản lý toàn diện hợp đồng lao động, phụ lục và thanh lý tuân thủ BLLĐ 2019")
public class ContractController {

    private final ContractService contractService;
    private final ContractTemplateService templateService;

    @PostMapping
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Soạn thảo hợp đồng lao động mới (Dự thảo DRAFT)")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> createContract(
            @Valid @RequestBody CreateContractRequest request
    ) {
        ContractDetailResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Soạn thảo hợp đồng lao động thành công"));
    }

    @GetMapping
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy danh sách hợp đồng lao động có phân trang")
    public ResponseEntity<ApiResponse<PageData<ContractResponse>>> getContracts(
            @Valid @ModelAttribute ContractFilter filter
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                contractService.getContracts(filter),
                "Lấy danh sách hợp đồng thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Xem chi tiết hợp đồng lao động kèm phụ lục và thông tin thanh lý")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> getContractById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getContractById(id), "Lấy thông tin hợp đồng thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('contract.update')")
    @RequirePermission("contract.update")
    @Operation(summary = "Cập nhật dự thảo hợp đồng lao động (Chỉ cho phép khi ở trạng thái DRAFT)")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> updateDraftContract(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.updateDraftContract(id, request), "Cập nhật dự thảo hợp đồng thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('contract.delete')")
    @RequirePermission("contract.delete")
    @Operation(summary = "Xóa dự thảo hợp đồng (Chỉ áp dụng khi status = DRAFT)")
    public ResponseEntity<ApiResponse<Void>> deleteDraftContract(@PathVariable UUID id) {
        contractService.deleteDraftContract(id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa dự thảo hợp đồng thành công"));
    }

    @PostMapping("/{id}/submit-approval")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Gửi hợp đồng vào quy trình phê duyệt (DRAFT -> PENDING_APPROVAL)")
    public ResponseEntity<ApiResponse<ContractResponse>> submitContractForApproval(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.submitContractForApproval(id), "Đã gửi hợp đồng đi phê duyệt"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@perm.has('contract.approve')")
    @RequirePermission("contract.approve")
    @Operation(summary = "Phê duyệt hợp đồng lao động (PENDING_APPROVAL -> APPROVED)")
    public ResponseEntity<ApiResponse<ContractResponse>> approveContract(
            @PathVariable UUID id,
            @RequestParam UUID approverId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.approveContract(id, approverId), "Phê duyệt hợp đồng thành công"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("@perm.has('contract.manage')")
    @RequirePermission("contract.manage")
    @Operation(summary = "Kích hoạt hiệu lực hợp đồng (Chuyển ACTIVE, đồng bộ trạng thái nhân viên)")
    public ResponseEntity<ApiResponse<ContractResponse>> activateContract(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signDate,
            @RequestParam(required = false) UUID signerEmployeeId,
            @RequestParam(required = false) String signedContractUrl
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                contractService.activateContract(id, signDate, signerEmployeeId, signedContractUrl),
                "Kích hoạt hiệu lực hợp đồng thành công"));
    }

    @PostMapping("/{id}/addenda")
    @PreAuthorize("@perm.has('contract.manage')")
    @RequirePermission("contract.manage")
    @Operation(summary = "Ký Phụ lục hợp đồng điều chỉnh tiền lương, chức danh hoặc thời hạn (Điều 22)")
    public ResponseEntity<ApiResponse<ContractAddendumResponse>> addAddendum(
            @PathVariable UUID id,
            @Valid @RequestBody CreateContractAddendumRequest request
    ) {
        ContractAddendumResponse response = contractService.addAddendum(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Thêm phụ lục hợp đồng thành công"));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Tái ký / Gia hạn hợp đồng (Kiểm tra chặn vượt quá 2 lần HĐ xác định thời hạn - Điều 20.2)")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> renewContract(
            @PathVariable UUID id,
            @Valid @RequestBody RenewContractRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.renewContract(id, request), "Gia hạn / Tái ký hợp đồng thành công"));
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("@perm.has('contract.terminate')")
    @RequirePermission("contract.terminate")
    @Operation(summary = "Chấm dứt hợp đồng lao động theo 1 trong 13 căn cứ pháp lý & tính trợ cấp Điều 46, 47")
    public ResponseEntity<ApiResponse<ContractTerminationResponse>> terminateContract(
            @PathVariable UUID id,
            @Valid @RequestBody TerminateContractRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.terminateContract(id, request), "Chấm dứt hợp đồng lao động thành công"));
    }

    @GetMapping("/{id}/severance-estimate")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Dự báo trợ cấp thôi việc (Điều 46) hoặc mất việc làm (Điều 47)")
    public ResponseEntity<ApiResponse<SeveranceEstimateResponse>> estimateSeverance(
            @PathVariable UUID id,
            @RequestParam TerminationGround ground,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finalWorkingDate
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.estimateSeverance(id, ground, finalWorkingDate), "Tính toán dự báo trợ cấp thành công"));
    }

    @GetMapping("/expiring")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Danh sách hợp đồng sắp hết hạn trong 30-45 ngày")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getExpiringContracts(
            @RequestParam UUID companyId,
            @RequestParam(defaultValue = "45") int withinDays
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getExpiringContracts(companyId, withinDays), "Lấy danh sách hợp đồng sắp hết hạn thành công"));
    }

    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy toàn bộ lịch sử hợp đồng lao động của một nhân viên")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getContractsByEmployee(employeeId), "Lấy lịch sử hợp đồng nhân viên thành công"));
    }

    @GetMapping("/{id}/render-document")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Render văn bản hợp đồng Mail-Merge từ mẫu template")
    public ResponseEntity<ApiResponse<String>> renderDocument(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID templateId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.renderContractDocument(id, templateId), "Render văn bản hợp đồng thành công"));
    }
}

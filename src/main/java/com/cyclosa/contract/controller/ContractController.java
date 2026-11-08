package com.cyclosa.contract.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.contract.dto.ContractDto;
import com.cyclosa.contract.dto.ContractSigningDto;
import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.entity.ContractVersion;
import com.cyclosa.contract.entity.enums.ContractStatus;
import com.cyclosa.contract.entity.enums.ContractType;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.repository.ContractRepository;
import com.cyclosa.contract.repository.ContractVersionRepository;
import com.cyclosa.contract.service.ContractComplianceService;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.common.exception.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Quản lý hợp đồng lao động cốt lõi")
public class ContractController {

    private final ContractService contractService;
    private final ContractComplianceService complianceService;
    private final ContractRepository contractRepository;
    private final ContractVersionRepository versionRepository;

    @GetMapping("/contracts")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy danh sách hợp đồng phân trang")
    public ResponseEntity<ApiResponse<PageData<ContractDto.ContractSummaryResponse>>> getContracts(
            @RequestParam UUID companyId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageData<ContractDto.ContractSummaryResponse> result = contractService.getContracts(
                companyId, employeeId, contractType, status, PageRequest.of(Math.max(0, page), Math.max(1, size))
        );
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách hợp đồng thành công"));
    }

    @GetMapping("/contracts/expiring")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy danh sách hợp đồng sắp hết hạn theo mốc cảnh báo")
    public ResponseEntity<ApiResponse<List<ContractDto.ContractSummaryResponse>>> getExpiringContracts(
            @RequestParam UUID companyId,
            @RequestParam(defaultValue = "30") int withinDays
    ) {
        List<ContractDto.ContractSummaryResponse> result = contractService.getExpiringContracts(companyId, withinDays);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách hợp đồng sắp hết hạn thành công"));
    }

    @PostMapping("/contracts")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Soạn thảo và khởi tạo hợp đồng lao động mới")
    public ResponseEntity<ApiResponse<ContractDto.ContractDetailResponse>> createContract(
            @Valid @RequestBody ContractDto.CreateContractRequest request
    ) {
        ContractDto.ContractDetailResponse result = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Khởi tạo hợp đồng lao động thành công"));
    }

    @GetMapping("/contracts/{contractId}")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Xem hồ sơ chi tiết hợp đồng lao động")
    public ResponseEntity<ApiResponse<ContractDto.ContractDetailResponse>> getContractById(
            @PathVariable UUID contractId
    ) {
        ContractDto.ContractDetailResponse result = contractService.getContractById(contractId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy chi tiết hợp đồng thành công"));
    }

    @PutMapping("/contracts/{contractId}")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Sửa đổi điều khoản hợp đồng (Chỉ áp dụng khi DRAFT)")
    public ResponseEntity<ApiResponse<ContractDto.ContractDetailResponse>> updateContract(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractDto.UpdateContractRequest request
    ) {
        ContractDto.ContractDetailResponse result = contractService.updateContract(contractId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật điều khoản hợp đồng thành công"));
    }

    @PutMapping("/contracts/{contractId}/allowances")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Thay thế danh mục phụ cấp thỏa thuận trong hợp đồng")
    public ResponseEntity<ApiResponse<ContractDto.ContractDetailResponse>> updateAllowances(
            @PathVariable UUID contractId,
            @Valid @RequestBody List<ContractDto.AllowanceDto> allowances
    ) {
        ContractDto.ContractDetailResponse result = contractService.updateContract(
                contractId, ContractDto.UpdateContractRequest.builder().allowances(allowances).build()
        );
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật phụ cấp hợp đồng thành công"));
    }

    @PostMapping("/contracts/{contractId}/compliance-check")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Tự động kiểm tra tuân thủ Bộ luật Lao động 2019")
    public ResponseEntity<ApiResponse<ContractDto.ComplianceCheckResponse>> checkCompliance(
            @PathVariable UUID contractId
    ) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_NOT_FOUND));
        ContractDto.ComplianceCheckResponse result = complianceService.checkCompliance(contract);
        return ResponseEntity.ok(ApiResponse.ok(result, "Kiểm tra tuân thủ hợp đồng thành công"));
    }

    @PostMapping("/contracts/{contractId}/submit")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Trình duyệt nội bộ hợp đồng lao động")
    public ResponseEntity<ApiResponse<Void>> submitContract(@PathVariable UUID contractId) {
        contractService.submitContract(contractId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã trình duyệt hợp đồng thành công"));
    }

    @PostMapping("/contracts/{contractId}/send-for-signature")
    @PreAuthorize("@perm.has('contract.sign_manage')")
    @RequirePermission("contract.sign_manage")
    @Operation(summary = "Phát hành bản ký và gửi thông báo mời các bên ký")
    public ResponseEntity<ApiResponse<Void>> sendForSignature(@PathVariable UUID contractId) {
        contractService.sendForSignature(contractId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã phát hành hợp đồng sang giai đoạn ký kết"));
    }

    @PostMapping("/contracts/{contractId}/cancel")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Hủy hợp đồng trước khi hoàn tất ký")
    public ResponseEntity<ApiResponse<Void>> cancelContract(@PathVariable UUID contractId) {
        contractService.cancelContract(contractId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã hủy hợp đồng thành công"));
    }

    @PostMapping("/contracts/{contractId}/renew")
    @PreAuthorize("@perm.has('contract.create')")
    @RequirePermission("contract.create")
    @Operation(summary = "Tái ký hợp đồng mới kế tiếp")
    public ResponseEntity<ApiResponse<ContractDto.ContractDetailResponse>> renewContract(@PathVariable UUID contractId) {
        ContractDto.ContractDetailResponse result = contractService.renewContract(contractId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Khởi tạo hợp đồng tái ký thành công"));
    }

    @PostMapping("/contracts/{contractId}/archive")
    @PreAuthorize("@perm.has('contract.archive')")
    @RequirePermission("contract.archive")
    @Operation(summary = "Đồng bộ hợp đồng đã ký vào kho tài liệu số")
    public ResponseEntity<ApiResponse<Void>> archiveContract(@PathVariable UUID contractId) {
        contractService.archiveContract(contractId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã lưu trữ hồ sơ hợp đồng vào kho tài liệu"));
    }

    @GetMapping("/employees/{employeeId}/contracts")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Xem toàn bộ lịch sử hợp đồng của một nhân viên")
    public ResponseEntity<ApiResponse<List<ContractDto.ContractSummaryResponse>>> getContractsByEmployee(
            @PathVariable UUID employeeId
    ) {
        List<ContractDto.ContractSummaryResponse> result = contractService.getContractsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy lịch sử hợp đồng nhân viên thành công"));
    }

    @GetMapping("/contracts/{contractId}/versions")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy các phiên bản tệp PDF của hợp đồng")
    public ResponseEntity<ApiResponse<List<ContractSigningDto.ContractVersionResponse>>> getContractVersions(
            @PathVariable UUID contractId
    ) {
        List<ContractSigningDto.ContractVersionResponse> result = versionRepository
                .findByContractIdAndAppendixIdIsNullOrderByVersionNoDesc(contractId).stream()
                .map(v -> ContractSigningDto.ContractVersionResponse.builder()
                        .id(v.getId())
                        .contractId(v.getContract().getId())
                        .versionNo(v.getVersionNo())
                        .fileUrl(v.getFileUrl())
                        .sha256Hash(v.getSha256Hash())
                        .changeReason(v.getChangeReason())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách phiên bản tệp thành công"));
    }

    @GetMapping("/contract-versions/{versionId}/download")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Tải xuống tệp PDF hợp đồng")
    public ResponseEntity<ApiResponse<Map<String, String>>> downloadContractVersion(@PathVariable UUID versionId) {
        ContractVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new AppException(ContractErrorCode.VERSION_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("downloadUrl", version.getFileUrl(), "sha256", version.getSha256Hash()),
                "Lấy đường dẫn tải văn bản thành công"
        ));
    }
}


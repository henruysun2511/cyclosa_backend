package com.cyclosa.contract.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.contract.dto.ContractSigningDto;
import com.cyclosa.contract.service.ContractSigningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Contract Signing", description = "Quy trình ký điện tử E-Contract và bằng chứng giao kết")
public class ContractSignerController {

    private final ContractSigningService signingService;

    @GetMapping("/contract-versions/{versionId}/signers")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Lấy danh sách các bên ký, thứ tự và tiến độ ký")
    public ResponseEntity<ApiResponse<List<ContractSigningDto.ContractSignerResponse>>> getSigners(
            @PathVariable UUID versionId
    ) {
        List<ContractSigningDto.ContractSignerResponse> result = signingService.getSignersByVersion(versionId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách bên ký thành công"));
    }

    @PostMapping("/contract-signers/{signerId}/request-otp")
    @Operation(summary = "Gửi mã OTP xác thực ký điện tử qua Email/SMS")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@PathVariable UUID signerId) {
        signingService.requestOtp(signerId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Mã OTP xác thực ký hợp đồng đã được gửi thành công"));
    }

    @PostMapping("/contract-signers/{signerId}/sign")
    @Operation(summary = "Ký hợp đồng điện tử bằng OTP hoặc Chữ ký số")
    public ResponseEntity<ApiResponse<ContractSigningDto.ContractSignerResponse>> sign(
            @PathVariable UUID signerId,
            @Valid @RequestBody ContractSigningDto.SignRequest request
    ) {
        ContractSigningDto.ContractSignerResponse result = signingService.sign(signerId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Ký hợp đồng điện tử thành công"));
    }

    @PostMapping("/contract-signers/{signerId}/decline")
    @Operation(summary = "Từ chối ký hợp đồng điện tử kèm lý do")
    public ResponseEntity<ApiResponse<Void>> decline(
            @PathVariable UUID signerId,
            @Valid @RequestBody ContractSigningDto.DeclineRequest request
    ) {
        signingService.decline(signerId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã ghi nhận từ chối ký hợp đồng"));
    }

    @PostMapping("/contract-signers/{signerId}/confirm-wet-ink")
    @PreAuthorize("@perm.has('contract.sign_manage')")
    @RequirePermission("contract.sign_manage")
    @Operation(summary = "HR xác nhận nhân viên đã ký trực tiếp trên bản giấy")
    public ResponseEntity<ApiResponse<Void>> confirmWetInk(@PathVariable UUID signerId) {
        signingService.confirmWetInk(signerId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xác nhận ký giấy thành công"));
    }

    @GetMapping("/contract-signers/{signerId}/evidence")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Tra cứu chứng cứ ký: mã giao dịch, số Serial chứng thư, dấu thời gian RFC 3161")
    public ResponseEntity<ApiResponse<ContractSigningDto.SigningEvidenceResponse>> getEvidence(
            @PathVariable UUID signerId
    ) {
        ContractSigningDto.SigningEvidenceResponse result = signingService.getEvidence(signerId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy chứng cứ giao kết thành công"));
    }

    @GetMapping("/contract-versions/{versionId}/verify-integrity")
    @PreAuthorize("@perm.has('contract.view')")
    @RequirePermission("contract.view")
    @Operation(summary = "Kiểm tra tính toàn vẹn văn bản bằng mã băm SHA-256")
    public ResponseEntity<ApiResponse<ContractSigningDto.VerifyIntegrityResponse>> verifyIntegrity(
            @PathVariable UUID versionId
    ) {
        ContractSigningDto.VerifyIntegrityResponse result = signingService.verifyIntegrity(versionId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Kiểm tra toàn vẹn văn bản thành công"));
    }
}


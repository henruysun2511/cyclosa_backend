package com.cyclosa.contract.dto;

import com.cyclosa.contract.entity.enums.CertificateValidity;
import com.cyclosa.contract.entity.enums.SignerSide;
import com.cyclosa.contract.entity.enums.SignerStatus;
import com.cyclosa.contract.entity.enums.SigningMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class ContractSigningDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignRequest {
        @NotNull(message = "Phương thức ký không được để trống")
        private SigningMethod method;

        private String otpCode;
        private String certificateSerial;
        private String certificateIssuer;
        private String signatureData;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeclineRequest {
        @NotBlank(message = "Lý do từ chối ký không được để trống")
        private String reason;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractSignerResponse {
        private UUID id;
        private UUID contractVersionId;
        private UUID signerEmployeeId;
        private String signerName;
        private String signerEmail;
        private SignerSide signerSide;
        private int signingOrder;
        private SigningMethod signingMethod;
        private SignerStatus status;
        private LocalDateTime signedAt;
        private String declineReason;
        private String transactionId;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SigningEvidenceResponse {
        private UUID signerId;
        private String transactionId;
        private String certificateSerial;
        private String certificateIssuer;
        private CertificateValidity certificateValidity;
        private String timestampToken;
        private String evidenceFileUrl;
        private LocalDateTime signedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractVersionResponse {
        private UUID id;
        private UUID contractId;
        private UUID appendixId;
        private int versionNo;
        private String fileUrl;
        private String sha256Hash;
        private String changeReason;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyIntegrityResponse {
        private boolean valid;
        private String fileHash;
        private String expectedHash;
        private String message;
    }
}


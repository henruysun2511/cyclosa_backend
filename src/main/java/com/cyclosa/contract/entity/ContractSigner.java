package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.entity.enums.CertificateValidity;
import com.cyclosa.contract.entity.enums.SignerSide;
import com.cyclosa.contract.entity.enums.SignerStatus;
import com.cyclosa.contract.entity.enums.SigningMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contract_signers", uniqueConstraints = {
        @UniqueConstraint(name = "uq_signer_version_order", columnNames = {"contract_version_id", "signing_order"}),
        @UniqueConstraint(name = "uq_signer_version_employee", columnNames = {"contract_version_id", "signer_employee_id"})
}, indexes = {
        @Index(name = "idx_contract_signers_version", columnList = "contract_version_id"),
        @Index(name = "idx_contract_signers_employee", columnList = "signer_employee_id"),
        @Index(name = "idx_contract_signers_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contract_signers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ContractSigner extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_version_id", nullable = false)
    private ContractVersion contractVersion;

    @Column(name = "signer_employee_id", nullable = false)
    private UUID signerEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "signer_side", nullable = false, length = 20)
    @Builder.Default
    private SignerSide signerSide = SignerSide.EMPLOYEE;

    @Column(name = "signing_order", nullable = false)
    @Builder.Default
    private int signingOrder = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "signing_method", nullable = false, length = 30)
    @Builder.Default
    private SigningMethod signingMethod = SigningMethod.OTP_CONFIRMATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SignerStatus status = SignerStatus.PENDING;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "decline_reason", length = 500)
    private String declineReason;

    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Column(name = "otp_expired_at")
    private LocalDateTime otpExpiredAt;

    @Column(name = "certificate_serial", length = 100)
    private String certificateSerial;

    @Column(name = "certificate_issuer", length = 200)
    private String certificateIssuer;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_validity", length = 20)
    private CertificateValidity certificateValidity;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "timestamp_token", columnDefinition = "TEXT")
    private String timestampToken;

    @Column(name = "evidence_file_url", length = 500)
    private String evidenceFileUrl;
}


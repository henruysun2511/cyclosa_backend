package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.enums.WorkingHoursType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contracts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contract_number", "company_id"})
})
@SQLDelete(sql = "UPDATE contracts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends BaseEntity {

    @Column(name = "contract_number", nullable = false, length = 100)
    private String contractNumber;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 40)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 30)
    @Builder.Default
    private ContractStatus contractStatus = ContractStatus.DRAFT;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "sign_date")
    private LocalDate signDate;

    @Column(name = "signer_employee_id")
    private UUID signerEmployeeId;

    /**
     * Mức lương cơ bản theo hợp đồng (dùng trích đóng BHXH, BHYT, BHTN bắt buộc).
     */
    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    /**
     * Lương đóng bảo hiểm (nếu doanh nghiệp quy định mức đóng riêng căn cứ mức tối đa BHXH).
     */
    @Column(name = "insurance_salary", precision = 15, scale = 2)
    private BigDecimal insuranceSalary;

    @Column(name = "allowance_lunch", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowanceLunch = BigDecimal.ZERO;

    @Column(name = "allowance_phone", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowancePhone = BigDecimal.ZERO;

    @Column(name = "allowance_transport", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowanceTransport = BigDecimal.ZERO;

    @Column(name = "allowance_other", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowanceOther = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_hours_type", length = 30)
    @Builder.Default
    private WorkingHoursType workingHoursType = WorkingHoursType.STANDARD_44H;

    @Column(name = "work_location_address", length = 300)
    private String workLocationAddress;

    @Column(name = "signed_contract_url", length = 500)
    private String signedContractUrl;

    /**
     * Thứ tự lần ký HĐ xác định thời hạn (1, 2). Phục vụ chặn cứng Điều 20.2 BLLĐ 2019.
     */
    @Column(name = "definite_contract_sequence")
    private Integer definiteContractSequence;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ContractAddendum> addenda = new ArrayList<>();

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ContractTermination termination;
}

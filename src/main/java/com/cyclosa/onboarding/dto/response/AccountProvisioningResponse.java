package com.cyclosa.onboarding.dto.response;

import com.cyclosa.onboarding.enums.ProvisioningStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tài khoản hệ thống cấp phát cho nhân sự")
public class AccountProvisioningResponse {

    private UUID id;
    private UUID companyId;
    private UUID onboardingProcessId;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String systemName;
    private String accountUsername;
    private ProvisioningStatus status;
    private String statusDescription;
    private UUID provisionedByEmployeeId;
    private String provisionedByEmployeeName;
    private LocalDateTime provisionedAt;
    private LocalDateTime revokedAt;
    private String notes;
}

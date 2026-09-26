package com.cyclosa.onboarding.dto.response;

import com.cyclosa.onboarding.enums.EmployeeDocumentType;
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
@Schema(description = "Thông tin tài liệu hồ sơ nhân sự")
public class EmployeeDocumentResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeDocumentType documentType;
    private String documentTypeDescription;
    private String documentName;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private Boolean isVerified;
    private UUID verifiedByEmployeeId;
    private String verifiedByEmployeeName;
    private LocalDateTime verifiedAt;
    private String notes;
}

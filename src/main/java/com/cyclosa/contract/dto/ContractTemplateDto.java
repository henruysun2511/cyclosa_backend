package com.cyclosa.contract.dto;

import com.cyclosa.contract.entity.enums.ContractType;
import com.cyclosa.contract.entity.enums.TemplateFormat;
import com.cyclosa.contract.entity.enums.TemplateKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class ContractTemplateDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTemplateRequest {
        @NotNull(message = "Công ty áp dụng không được để trống")
        private UUID companyId;

        @NotBlank(message = "Tên mẫu hợp đồng không được để trống")
        private String name;

        private TemplateKind templateKind;
        private TemplateKind kind;
        private String templateCode;

        private ContractType contractType;
        private UUID jobTitleId;
        private UUID departmentId;
        private UUID branchId;

        private TemplateFormat format;
        private String contentRaw;
        private String sampleDataJson;
        private String description;

        public TemplateKind getEffectiveTemplateKind() {
            if (templateKind != null) return templateKind;
            if (kind != null) return kind;
            return TemplateKind.LABOR_CONTRACT;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTemplateRequest {
        @NotBlank(message = "Tên mẫu hợp đồng không được để trống")
        private String name;

        private TemplateKind templateKind;
        private TemplateKind kind;

        private ContractType contractType;
        private UUID jobTitleId;
        private UUID departmentId;
        private UUID branchId;
        private Boolean active;
        private String description;

        public TemplateKind getEffectiveTemplateKind() {
            if (templateKind != null) return templateKind;
            if (kind != null) return kind;
            return TemplateKind.LABOR_CONTRACT;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateVersionRequest {
        private TemplateFormat format;
        private String fileUrl;
        private String description;
        private String contentRaw;
        private String sampleDataJson;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RenderPreviewRequest {
        private UUID templateId;
        private UUID versionId;
        private String contentRaw;
        private String contentPayload;
        private String sampleDataJson;
        private java.util.Map<String, Object> data;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateResponse {
        private UUID id;
        private UUID companyId;
        private String name;
        private TemplateKind templateKind;
        private ContractType contractType;
        private UUID jobTitleId;
        private String jobTitleName;
        private UUID departmentId;
        private String departmentName;
        private UUID branchId;
        private String branchName;
        private boolean active;
        private int versionCount;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateVersionResponse {
        private UUID id;
        private UUID templateId;
        private int versionNo;
        private TemplateFormat format;
        private String fileUrl;
        private String description;
        private LocalDateTime createdAt;
    }
}


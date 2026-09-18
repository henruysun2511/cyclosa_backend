package com.cyclosa.contract.dto.response;

import com.cyclosa.contract.enums.ContractType;
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
public class ContractTemplateResponse {

    private UUID id;
    private UUID companyId;
    private String templateCode;
    private String title;
    private ContractType contractType;
    private String contractTypeDescription;
    private String templateContent;
    private Integer version;
    private boolean active;
    private String description;
    private LocalDateTime createdAt;
}

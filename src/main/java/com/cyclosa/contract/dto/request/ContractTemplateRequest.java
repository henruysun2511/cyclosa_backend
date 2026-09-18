package com.cyclosa.contract.dto.request;

import com.cyclosa.contract.enums.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTemplateRequest {

    @NotNull(message = "ID công ty không được để trống")
    private UUID companyId;

    @NotBlank(message = "Mã mẫu hợp đồng không được để trống")
    private String templateCode;

    @NotBlank(message = "Tiêu đề mẫu không được để trống")
    private String title;

    @NotNull(message = "Loại hợp đồng không được để trống")
    private ContractType contractType;

    @NotBlank(message = "Nội dung template không được để trống")
    private String templateContent;

    private String description;
}

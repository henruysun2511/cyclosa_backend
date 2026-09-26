package com.cyclosa.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKpiRequest {

    private UUID organizationalUnitId;

    private UUID companyId;

    @NotBlank(message = "Tên chỉ tiêu KPI không được để trống")
    private String name;

    @NotBlank(message = "Đơn vị đo lường không được để trống (%, VND, Số lượng, ...)")
    private String unit;

    private String description;
}

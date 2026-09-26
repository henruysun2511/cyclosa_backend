package com.cyclosa.performance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKpiRequest {

    private UUID organizationalUnitId;

    private String name;

    private String unit;

    private String description;

    private Boolean isActive;
}

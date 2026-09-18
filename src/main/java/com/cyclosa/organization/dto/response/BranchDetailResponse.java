package com.cyclosa.organization.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.RegionSummary;
import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết toàn diện của chi nhánh (kèm tọa độ GPS & bán kính chấm công)")
public class BranchDetailResponse {

    private UUID id;
    private CompanySummary company;
    private RegionSummary region;
    private String code;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer checkinRadiusMeters;
    private ActiveStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

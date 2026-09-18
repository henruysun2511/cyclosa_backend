package com.cyclosa.organization.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.JobLevelSummary;
import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết toàn diện của vị trí chức danh")
public class PositionDetailResponse {
    private UUID id;
    private CompanySummary company;
    private String code;
    private String name;
    private JobLevelSummary jobLevel;
    private String description;
    private ActiveStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

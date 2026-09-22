package com.cyclosa.attendance.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết chuyên sâu ca làm việc")
public class ShiftDetailResponse {

    private UUID id;
    private UUID companyId;
    private CompanySummary company;
    private String code;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private BigDecimal workingHours;
    private BigDecimal workUnits;
    private Integer graceLateMinutes;
    private Integer graceEarlyMinutes;
    private Boolean isNightShift;
    private Boolean isActive;
    private long activeAssignmentsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

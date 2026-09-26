package com.cyclosa.performance.dto.response;

import com.cyclosa.performance.enums.PerformanceCycleStatus;
import com.cyclosa.performance.enums.PerformanceCycleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceCycleResponse {

    private UUID id;

    private String name;

    private PerformanceCycleType cycleType;

    private LocalDate startDate;

    private LocalDate endDate;

    private PerformanceCycleStatus status;

    private UUID companyId;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

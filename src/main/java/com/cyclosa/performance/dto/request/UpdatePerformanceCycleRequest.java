package com.cyclosa.performance.dto.request;

import com.cyclosa.performance.enums.PerformanceCycleStatus;
import com.cyclosa.performance.enums.PerformanceCycleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePerformanceCycleRequest {

    private String name;

    private PerformanceCycleType cycleType;

    private LocalDate startDate;

    private LocalDate endDate;

    private PerformanceCycleStatus status;

    private String description;
}

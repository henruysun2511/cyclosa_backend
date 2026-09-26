package com.cyclosa.performance.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.performance.enums.GoalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin mục tiêu hiệu suất")
public class GoalResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private EmployeeSummary employee;

    private UUID performanceCycleId;

    private String performanceCycleName;

    private UUID kpiId;

    private String kpiName;

    private String kpiUnit;

    private String title;

    private String targetValue;

    private BigDecimal weightPercentage;

    private GoalStatus status;

    private String description;

    private LocalDateTime createdAt;
}

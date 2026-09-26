package com.cyclosa.performance.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.performance.enums.EvaluationStatus;
import com.cyclosa.performance.enums.PerformanceRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết phiếu đánh giá hiệu suất kèm các mục tiêu và đánh giá")
public class PerformanceEvaluationDetailResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private EmployeeSummary employee;

    private String departmentName;

    private OrgUnitSummary department;

    private String positionName;

    private PositionSummary position;

    private UUID performanceCycleId;

    private String performanceCycleName;

    private EvaluationStatus status;

    private BigDecimal finalScore;

    private PerformanceRating rating;

    private String selfOverallComment;

    private String managerOverallComment;

    private LocalDateTime selfSubmittedAt;

    private LocalDateTime managerSubmittedAt;

    private UUID finalizedByEmployeeId;

    private String finalizedByEmployeeName;

    private EmployeeSummary finalizedBy;

    private LocalDateTime finalizedAt;

    private List<GoalDetailResponse> goals;
}

package com.cyclosa.offboarding.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Chi tiết phỏng vấn thôi việc (Exit Interview)")
public class ExitInterviewDetailResponse {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private EmployeeSummary employee;

    private UUID companyId;
    private CompanySummary company;

    private LocalDate interviewDate;
    private UUID interviewerEmployeeId;
    private String interviewerEmployeeName;
    private EmployeeSummary interviewerEmployee;

    private String feedbackSummary;
    private Boolean wouldRecommendCompany;
    private String reasonForLeaving;
    private String suggestions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.AssignmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalAssignmentResponse {

    private UUID id;

    private UUID companyId;

    private UUID opportunityId;

    private String opportunityTitle;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private AssignmentStatus status;

    private BigDecimal performanceRating;

    private String evaluationNote;

    private LocalDateTime createdAt;
}

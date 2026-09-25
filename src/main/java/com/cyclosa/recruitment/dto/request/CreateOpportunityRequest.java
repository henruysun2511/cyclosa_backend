package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.OpportunityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOpportunityRequest {

    @NotBlank(message = "Tiêu đề cơ hội nội bộ không được để trống")
    private String title;

    @NotNull(message = "Loại cơ hội không được để trống")
    private OpportunityType type;

    private UUID departmentId;

    private UUID managerId;

    private String description;

    private String requiredSkills;

    private Integer commitmentPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private UUID jobPositionId;

    private Integer targetHires;
}

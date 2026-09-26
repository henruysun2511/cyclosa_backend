package com.cyclosa.discipline.dto.request;

import com.cyclosa.discipline.enums.DisciplineStatus;
import com.cyclosa.discipline.enums.DisciplineType;
import com.cyclosa.discipline.enums.DismissalGround;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDisciplineRequest {

    private DisciplineType disciplineType;

    private DismissalGround dismissalGround;

    private Integer salaryExtensionMonths;

    private String reason;

    private LocalDate meetingDate;

    private String meetingAttendees;

    private LocalDate decisionDate;

    private UUID decidedByEmployeeId;

    private DisciplineStatus status;

    private String evidenceFiles;

    private String fileUrl;
}

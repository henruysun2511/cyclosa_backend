package com.cyclosa.discipline.dto.request;

import com.cyclosa.discipline.enums.DisciplineStatus;
import com.cyclosa.discipline.enums.DisciplineType;
import com.cyclosa.discipline.enums.DismissalGround;
import com.cyclosa.discipline.enums.ViolationCategory;
import jakarta.validation.constraints.NotNull;
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
public class CreateDisciplineRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    private UUID companyId;

    @NotNull(message = "Ngày vi phạm không được để trống")
    @jakarta.validation.constraints.PastOrPresent(message = "Ngày vi phạm không thể ở tương lai")
    private LocalDate violationDate;

    @NotNull(message = "Nhóm hành vi vi phạm không được để trống")
    private ViolationCategory violationCategory;

    private String evidenceFiles;

    private String handbookReference;

    private LocalDate meetingDate;

    private String meetingAttendees;

    @NotNull(message = "Hình thức kỷ luật không được để trống")
    private DisciplineType disciplineType;

    private DismissalGround dismissalGround;

    private Integer salaryExtensionMonths;

    private String reason;

    private LocalDate decisionDate;

    private UUID decidedByEmployeeId;

    private DisciplineStatus status;

    private String fileUrl;
}

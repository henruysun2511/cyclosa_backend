package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {

    @NotNull(message = "ID cơ hội không được để trống")
    private UUID opportunityId;

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    private LocalDate startDate;

    private LocalDate endDate;
}

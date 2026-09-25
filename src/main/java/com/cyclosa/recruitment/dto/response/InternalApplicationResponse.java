package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.InternalApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalApplicationResponse {

    private UUID id;

    private UUID companyId;

    private UUID opportunityId;

    private String opportunityTitle;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private String note;

    private InternalApplicationStatus status;

    private String feedback;

    private LocalDateTime createdAt;
}

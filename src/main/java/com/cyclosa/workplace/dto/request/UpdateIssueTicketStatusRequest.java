package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIssueTicketStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private IssueStatus status;

    private UUID assignedToUserId;

    private String resolutionNotes;
}

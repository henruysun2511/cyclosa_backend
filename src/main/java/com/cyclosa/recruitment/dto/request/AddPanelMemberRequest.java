package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPanelMemberRequest {

    @NotNull(message = "ID nhân viên phỏng vấn không được để trống")
    private UUID interviewerEmployeeId;

    private String role;
}

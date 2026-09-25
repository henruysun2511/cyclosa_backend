package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToTalentPoolRequest {
    private UUID companyId;
    @NotNull(message = "ID ứng viên không được để trống")
    private UUID candidateId;
    private String tag;
    private String source;
    private String notes;
}

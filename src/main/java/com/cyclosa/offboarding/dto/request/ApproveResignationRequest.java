package com.cyclosa.offboarding.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveResignationRequest {

    @NotNull(message = "Kết quả phê duyệt không được để trống")
    private Boolean isApproved;

    private String rejectionReason;

    private LocalDate effectiveLastWorkingDate;
}

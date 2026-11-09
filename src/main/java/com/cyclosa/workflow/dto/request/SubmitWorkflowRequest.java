package com.cyclosa.workflow.dto.request;

import com.cyclosa.workflow.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitWorkflowRequest {

    @NotNull(message = "Công ty áp dụng không được để trống")
    private UUID companyId;

    @NotNull(message = "Loại chứng từ không được để trống")
    private RequestType requestType;

    @NotNull(message = "ID chứng từ không được để trống")
    private UUID referenceId;

    private String referenceCode;

    @NotBlank(message = "Tiêu đề yêu cầu không được để trống")
    private String title;

    private UUID requesterEmployeeId;

    private BigDecimal requestAmount;

    private String metadata;
}


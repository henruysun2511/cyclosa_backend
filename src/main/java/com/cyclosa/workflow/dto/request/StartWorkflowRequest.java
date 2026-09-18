package com.cyclosa.workflow.dto.request;

import com.cyclosa.workflow.enums.ApprovalRequestType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartWorkflowRequest {

    @NotNull(message = "Company ID không được để trống")
    private UUID companyId;

    @NotNull(message = "Loại yêu cầu không được để trống")
    private ApprovalRequestType requestType;

    @NotNull(message = "ID bản ghi nghiệp vụ không được để trống")
    private UUID requestId;

    @NotNull(message = "ID nhân viên nộp đơn không được để trống")
    private UUID requesterEmployeeId;

    @Builder.Default
    private Map<String, Object> contextVariables = new HashMap<>();
}

package com.cyclosa.workflow.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.workflow.enums.RequestType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WorkflowDefinitionFilterRequest extends BaseFilterRequest {
    private UUID companyId;
    private RequestType requestType;
    private Boolean isActive;
}


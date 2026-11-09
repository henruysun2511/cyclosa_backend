package com.cyclosa.workflow.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.workflow.enums.RequestType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowInboxFilterRequest extends BaseFilterRequest {
    private RequestType requestType;
}


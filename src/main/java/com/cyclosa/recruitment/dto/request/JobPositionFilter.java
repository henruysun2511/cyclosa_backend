package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.JobPositionStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class JobPositionFilter extends BaseFilterRequest {
    private UUID departmentId;
    private UUID positionId;
    private JobPositionStatus status;
}

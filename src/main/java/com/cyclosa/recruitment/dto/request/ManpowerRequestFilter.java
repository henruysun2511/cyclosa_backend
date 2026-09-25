package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.ManpowerRequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ManpowerRequestFilter extends BaseFilterRequest {
    private UUID departmentId;
    private UUID positionId;
    private ManpowerRequestStatus status;
}

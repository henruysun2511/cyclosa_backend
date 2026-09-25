package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.ApplicationStage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ApplicationFilter extends BaseFilterRequest {
    private UUID jobPositionId;
    private UUID jobPostingId;
    private UUID candidateId;
    private ApplicationStage stage;
}

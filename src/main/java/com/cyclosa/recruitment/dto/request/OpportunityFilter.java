package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.OpportunityStatus;
import com.cyclosa.recruitment.enums.OpportunityType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OpportunityFilter extends BaseFilterRequest {

    private OpportunityType type;

    private OpportunityStatus status;

    private UUID departmentId;

    private String skill;
}

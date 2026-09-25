package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.CandidateSource;
import com.cyclosa.recruitment.enums.CandidateStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidateFilter extends BaseFilterRequest {
    private CandidateSource source;
    private CandidateStatus status;
}

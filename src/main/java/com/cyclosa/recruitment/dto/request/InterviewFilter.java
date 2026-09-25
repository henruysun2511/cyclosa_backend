package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.InterviewStatus;
import com.cyclosa.recruitment.enums.InterviewType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InterviewFilter extends BaseFilterRequest {

    private UUID applicationId;

    private InterviewType interviewType;

    private InterviewStatus status;
}

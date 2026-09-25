package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.PostingChannel;
import com.cyclosa.recruitment.enums.PostingStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class JobPostingFilter extends BaseFilterRequest {
    private UUID jobPositionId;
    private PostingChannel channel;
    private PostingStatus status;
}

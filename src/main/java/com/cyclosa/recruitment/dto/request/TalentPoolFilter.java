package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TalentPoolFilter extends BaseFilterRequest {
    private String tag;
}

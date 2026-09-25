package com.cyclosa.recruitment.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.recruitment.enums.OfferStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OfferFilter extends BaseFilterRequest {

    private UUID applicationId;

    private OfferStatus status;
}

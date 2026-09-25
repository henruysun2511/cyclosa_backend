package com.cyclosa.recruitment.mapper;

import com.cyclosa.recruitment.dto.request.CreateOfferRequest;
import com.cyclosa.recruitment.dto.response.OfferDetailResponse;
import com.cyclosa.recruitment.dto.response.OfferResponse;
import com.cyclosa.recruitment.entity.Offer;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OfferMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "declineReason", ignore = true)
    Offer toEntity(CreateOfferRequest request);

    @Mapping(target = "candidateName", ignore = true)
    @Mapping(target = "candidateEmail", ignore = true)
    @Mapping(target = "jobPositionTitle", ignore = true)
    OfferResponse toResponse(Offer entity);

    @Mapping(target = "candidateName", ignore = true)
    @Mapping(target = "candidateEmail", ignore = true)
    @Mapping(target = "candidatePhone", ignore = true)
    @Mapping(target = "jobPositionTitle", ignore = true)
    OfferDetailResponse toDetailResponse(Offer entity);
}

package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.OfferStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespondOfferRequest {

    @NotNull(message = "Trạng thái phản hồi Offer không được để trống (ACCEPTED hoặc DECLINED)")
    private OfferStatus status;

    private String declineReason;
}

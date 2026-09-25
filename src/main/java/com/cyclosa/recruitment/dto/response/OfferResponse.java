package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.OfferStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResponse {

    private UUID id;

    private UUID companyId;

    private UUID applicationId;

    private String candidateName;

    private String candidateEmail;

    private String jobPositionTitle;

    private BigDecimal offeredSalary;

    private LocalDate startDate;

    private LocalDate expirationDate;

    private OfferStatus status;

    private LocalDateTime createdAt;
}

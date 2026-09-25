package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOfferRequest {

    private UUID companyId;

    @NotNull(message = "Hồ sơ ứng tuyển không được để trống")
    private UUID applicationId;

    @NotNull(message = "Mức lương đề xuất không được để trống")
    private BigDecimal offeredSalary;

    @NotNull(message = "Ngày bắt đầu làm việc dự kiến không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Hạn phản hồi Offer không được để trống")
    private LocalDate expirationDate;

    private String benefitsNotes;

    private String notes;
}

package com.cyclosa.discipline.dto.request;

import com.cyclosa.discipline.enums.RewardType;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRewardRequest {

    private RewardType rewardType;

    private String title;

    @Positive(message = "Số tiền khen thưởng phải lớn hơn 0")
    private BigDecimal amount;

    private String reason;

    private UUID decidedByEmployeeId;

    @PastOrPresent(message = "Ngày quyết định khen thưởng không thể là ngày trong tương lai")
    private LocalDate decidedDate;
}

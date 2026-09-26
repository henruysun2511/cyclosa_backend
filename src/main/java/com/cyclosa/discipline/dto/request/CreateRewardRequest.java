package com.cyclosa.discipline.dto.request;

import com.cyclosa.discipline.enums.RewardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateRewardRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    private UUID companyId;

    @NotNull(message = "Hình thức khen thưởng không được để trống")
    private RewardType rewardType;

    @NotBlank(message = "Tiêu đề khen thưởng không được để trống")
    private String title;

    @jakarta.validation.constraints.Positive(message = "Số tiền khen thưởng phải lớn hơn 0")
    private BigDecimal amount;

    private String reason;

    private UUID decidedByEmployeeId;

    @NotNull(message = "Ngày quyết định không được để trống")
    @jakarta.validation.constraints.PastOrPresent(message = "Ngày quyết định khen thưởng không thể là ngày trong tương lai")
    private LocalDate decidedDate;
}

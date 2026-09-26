package com.cyclosa.offboarding.dto.request;

import com.cyclosa.offboarding.enums.ResignationReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyResignationRequest {

    @NotNull(message = "Ngày dự kiến nghỉ việc không được để trống")
    @jakarta.validation.constraints.Future(message = "Ngày dự kiến nghỉ việc phải là một ngày trong tương lai")
    private LocalDate expectedLastWorkingDate;

    @NotNull(message = "Lý do xin thôi việc không được để trống")
    private ResignationReason personalReasonCategory;

    private String reasonDetail;
}

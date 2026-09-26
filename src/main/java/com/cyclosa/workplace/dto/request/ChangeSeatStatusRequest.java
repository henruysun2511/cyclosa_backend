package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.SeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeSeatStatusRequest {

    @NotNull(message = "Trạng thái ghế không được để trống")
    private SeatStatus status;

    private String reason;
}

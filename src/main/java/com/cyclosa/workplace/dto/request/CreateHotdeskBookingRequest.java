package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHotdeskBookingRequest {

    @NotNull(message = "Chỗ ngồi (seatId) không được để trống")
    private UUID seatId;

    @NotNull(message = "Ngày đặt bàn không được để trống")
    private LocalDate bookingDate;

    private UUID employeeId;

    private String notes;
}

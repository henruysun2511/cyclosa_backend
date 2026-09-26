package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomBookingRequest {

    @NotNull(message = "Phòng họp (roomId) không được để trống")
    private UUID roomId;

    @NotBlank(message = "Tiêu đề cuộc họp không được để trống")
    @Size(max = 200, message = "Tiêu đề không vượt quá 200 ký tự")
    private String meetingTitle;

    @NotNull(message = "Ngày đặt phòng không được để trống")
    private LocalDate bookingDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    private Integer participantCount;

    private UUID bookedByEmployeeId;

    private String notes;
}

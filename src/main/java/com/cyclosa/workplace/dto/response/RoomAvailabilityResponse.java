package com.cyclosa.workplace.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityResponse {

    private UUID roomId;
    private String roomName;
    private Integer capacity;
    private LocalDate date;
    private List<TimeSlot> bookedSlots;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private UUID bookingId;
        private String meetingTitle;
        private LocalTime startTime;
        private LocalTime endTime;
        private String bookedByName;
    }
}

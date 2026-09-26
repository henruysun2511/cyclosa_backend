package com.cyclosa.workplace.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.workplace.enums.RoomBookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomBookingResponse {

    private UUID id;
    private UUID companyId;
    private UUID roomId;
    private String roomName;
    private Integer roomCapacity;
    private String floorNumber;
    private String zoneName;
    private UUID bookedByEmployeeId;
    private EmployeeSummary bookedByEmployee;
    private String meetingTitle;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer participantCount;
    private RoomBookingStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.cyclosa.workplace.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.workplace.enums.HotdeskBookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotdeskBookingResponse {

    private UUID id;
    private UUID companyId;
    private UUID seatId;
    private String seatCode;
    private String floorNumber;
    private String zoneName;
    private UUID employeeId;
    private EmployeeSummary employee;
    private LocalDate bookingDate;
    private HotdeskBookingStatus status;
    private LocalDateTime checkedInAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

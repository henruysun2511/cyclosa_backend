package com.cyclosa.workplace.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.workplace.enums.RoomBookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc danh sách đặt phòng họp")
public class RoomBookingFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo phòng họp (roomId)")
    private UUID roomId;

    @Schema(description = "Lọc theo người đặt (employeeId)")
    private UUID bookedByEmployeeId;

    @Schema(description = "Lọc theo ngày họp")
    private LocalDate bookingDate;

    @Schema(description = "Lọc theo trạng thái đặt phòng")
    private RoomBookingStatus status;
}

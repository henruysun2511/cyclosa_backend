package com.cyclosa.workplace.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.workplace.enums.HotdeskBookingStatus;
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
@Schema(description = "Bộ lọc danh sách đặt bàn linh hoạt hot-desk")
public class HotdeskBookingFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo nhân viên")
    private UUID employeeId;

    @Schema(description = "Lọc theo chỗ ngồi")
    private UUID seatId;

    @Schema(description = "Lọc theo tầng")
    private UUID floorId;

    @Schema(description = "Lọc theo ngày đặt")
    private LocalDate bookingDate;

    @Schema(description = "Lọc theo trạng thái đặt bàn")
    private HotdeskBookingStatus status;
}

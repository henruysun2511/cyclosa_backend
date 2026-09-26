package com.cyclosa.workplace.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.enums.SeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc danh sách chỗ ngồi làm việc")
public class WorkplaceSeatFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo tầng")
    private UUID floorId;

    @Schema(description = "Lọc theo khu vực")
    private UUID zoneId;

    @Schema(description = "Lọc theo dãy bàn")
    private UUID rowId;

    @Schema(description = "Lọc theo loại chỗ ngồi (DEDICATED, HOT_DESK, VISITOR)")
    private SeatType seatType;

    @Schema(description = "Lọc theo trạng thái chỗ ngồi (AVAILABLE, OCCUPIED, MAINTENANCE, BLOCKED)")
    private SeatStatus status;

    @Schema(description = "Lọc theo phòng ban phụ trách")
    private UUID departmentId;
}

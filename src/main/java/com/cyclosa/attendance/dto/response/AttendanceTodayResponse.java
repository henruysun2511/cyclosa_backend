package com.cyclosa.attendance.dto.response;

import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.enums.CheckMethod;
import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.ShiftSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trạng thái điểm danh hôm nay của cá nhân")
public class AttendanceTodayResponse {

    @Schema(description = "Ngày làm việc hôm nay")
    private LocalDate workDate;

    @Schema(description = "Thông tin ca làm việc phân bổ")
    private ShiftSummary shift;

    @Schema(description = "Chi nhánh trực thuộc")
    private BranchSummary branch;

    @Schema(description = "Thời điểm Check-in thực tế")
    private LocalDateTime checkInTime;

    @Schema(description = "Hình thức Check-in")
    private CheckMethod checkInMethod;

    @Schema(description = "Thời điểm Check-out thực tế")
    private LocalDateTime checkOutTime;

    @Schema(description = "Hình thức Check-out")
    private CheckMethod checkOutMethod;

    @Schema(description = "Số phút đi muộn")
    private Integer lateMinutes;

    @Schema(description = "Số phút về sớm")
    private Integer earlyMinutes;

    @Schema(description = "Số giờ làm việc thực tế")
    private BigDecimal actualHours;

    @Schema(description = "Số công được hưởng")
    private BigDecimal actualWorkUnits;

    @Schema(description = "Trạng thái điểm danh")
    private AttendanceStatus status;

    @Schema(description = "Đã check-in chưa")
    private boolean hasCheckedIn;

    @Schema(description = "Đã check-out chưa")
    private boolean hasCheckedOut;
}

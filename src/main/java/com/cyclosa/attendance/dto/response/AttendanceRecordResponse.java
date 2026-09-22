package com.cyclosa.attendance.dto.response;

import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.enums.CheckMethod;
import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.ShiftSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết bản ghi điểm danh")
public class AttendanceRecordResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeSummary employee;
    private BranchSummary branch;
    private ShiftSummary shift;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private Double checkInLat;
    private Double checkInLong;
    private CheckMethod checkInMethod;
    private LocalDateTime checkOutTime;
    private Double checkOutLat;
    private Double checkOutLong;
    private CheckMethod checkOutMethod;
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private BigDecimal actualHours;
    private BigDecimal actualWorkUnits;
    private AttendanceStatus status;
    private String note;
    private LocalDateTime createdAt;
}

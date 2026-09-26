package com.cyclosa.discipline.dto.response;

import com.cyclosa.discipline.enums.ViolationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả tra cứu thời hiệu xử lý kỷ luật lao động theo Điều 123 BLLĐ")
public class StatuteOfLimitationResponse {

    @Schema(description = "ID hồ sơ kỷ luật")
    private UUID disciplineId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Tên nhân viên")
    private String employeeName;

    @Schema(description = "Mã nhân viên")
    private String employeeCode;

    @Schema(description = "Ngày vi phạm")
    private LocalDate violationDate;

    @Schema(description = "Phân loại vi phạm")
    private ViolationCategory violationCategory;

    @Schema(description = "Hạn chót thời hiệu xử lý kỷ luật")
    private LocalDate statuteOfLimitationDeadline;

    @Schema(description = "Đã quá thời hiệu xử lý kỷ luật hay chưa")
    private boolean isExpired;

    @Schema(description = "Số ngày còn lại trong thời hiệu (0 nếu đã quá hạn)")
    private long remainingDays;

    @Schema(description = "Căn cứ pháp lý áp dụng")
    private String legalBasis;

    @Schema(description = "Thông báo kết luận về thời hiệu")
    private String message;
}

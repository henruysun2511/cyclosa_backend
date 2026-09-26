package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin hồ sơ nhân sự trong kho nhân tài")
public class TalentPoolResponse {

    @Schema(description = "ID bản ghi kho nhân tài")
    private UUID id;

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Thông tin tóm tắt nhân viên")
    private EmployeeSummary employee;

    @Schema(description = "Thẻ phân loại năng lực (HiPo, Key Talent...)")
    private String tag;

    @Schema(description = "Ghi chú đánh giá, tiềm năng")
    private String note;

    @Schema(description = "ID người đề xuất / thêm vào kho")
    private UUID addedByEmployeeId;

    @Schema(description = "Thông tin người đề xuất")
    private EmployeeSummary addedByEmployee;

    @Schema(description = "Thời điểm đưa vào kho")
    private LocalDateTime createdAt;
}

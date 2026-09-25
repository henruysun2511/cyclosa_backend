package com.cyclosa.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết lỗi của một dòng khi import Excel")
public class EmployeeImportRowError {

    @Schema(description = "Số thứ tự dòng trong file Excel (1-based)", example = "5")
    private int rowNumber;

    @Schema(description = "Mã nhân viên tại dòng lỗi (nếu có)", example = "EMP-2026-0001")
    private String employeeCode;

    @Schema(description = "Họ và tên nhân viên tại dòng lỗi", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Số CCCD/Hộ chiếu tại dòng lỗi", example = "001234567890")
    private String nationalIdNumber;

    @Schema(description = "Lý do thất bại", example = "Số CCCD/Hộ chiếu đã được đăng ký cho nhân viên khác")
    private String reason;

    @Schema(description = "Chi tiết các trường bị lỗi")
    private List<String> fieldErrors;
}

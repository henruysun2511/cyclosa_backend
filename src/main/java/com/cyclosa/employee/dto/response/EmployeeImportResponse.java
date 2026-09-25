package com.cyclosa.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả nhập danh sách nhân viên hàng loạt bằng Excel")
public class EmployeeImportResponse {

    @Schema(description = "Tổng số dòng dữ liệu xử lý trong file Excel", example = "50")
    private int totalRows;

    @Schema(description = "Số lượng nhân viên nhập thành công", example = "48")
    private int successCount;

    @Schema(description = "Số lượng dòng bị lỗi không thể nhập", example = "2")
    private int failureCount;

    @Schema(description = "Danh sách nhân sự vừa tạo mới thành công")
    @Builder.Default
    private List<EmployeeResponse> successfulEmployees = new ArrayList<>();

    @Schema(description = "Danh sách chi tiết các dòng bị lỗi kèm nguyên nhân")
    @Builder.Default
    private List<EmployeeImportRowError> errors = new ArrayList<>();
}

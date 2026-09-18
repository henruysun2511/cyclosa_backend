package com.cyclosa.common.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt nhân sự (dùng cho quản lý trực tiếp, người duyệt...)")
public class EmployeeSummary {

    @Schema(description = "ID nhân viên")
    private UUID id;

    @Schema(description = "Mã nhân viên", example = "EMP00123")
    private String employeeCode;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Ảnh đại diện / Avatar")
    private String photoUrl;

    @Schema(description = "Email công ty", example = "a.nguyen@cyclosa.com")
    private String companyEmail;
}

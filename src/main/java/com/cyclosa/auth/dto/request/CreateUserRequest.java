package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo tài khoản nhân viên mới từ Quản trị viên")
public class CreateUserRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @Schema(description = "Email doanh nghiệp hoặc cá nhân của nhân viên", example = "nhanvien.a@cyclosa.com")
    private String email;

    @Schema(description = "ID nhân sự tương ứng (tùy chọn)", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID employeeId;

    @Schema(description = "Danh sách mã hoặc ID vai trò phân quyền ban đầu (ví dụ: EMPLOYEE, HR_ADMIN)", example = "[\"EMPLOYEE\"]")
    private List<String> roleCodes;
}


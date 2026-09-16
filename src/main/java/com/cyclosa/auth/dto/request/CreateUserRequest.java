package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo tài khoản nhân viên từ phía quản trị viên")
public class CreateUserRequest {

    @Schema(description = "ID bản ghi nhân viên tương ứng trong hệ thống HRM (nếu có)")
    private UUID employeeId;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Email làm việc của nhân viên", example = "hoang.nv@cyclosa.com")
    private String email;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 150, message = "Họ và tên từ 2 đến 150 ký tự")
    @Schema(description = "Họ và tên nhân viên", example = "Nguyễn Văn Hoàng")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Schema(description = "Số điện thoại", example = "0912345678")
    private String phone;

    @Schema(description = "Danh sách mã vai trò gán ban đầu (mặc định: EMPLOYEE)", example = "[\"EMPLOYEE\"]")
    private List<String> roleCodes;

    @Schema(description = "ID công ty mà vai trò thuộc về (nếu có)")
    private UUID companyId;
}

package com.cyclosa.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cấp phát tài khoản hệ thống cho nhân viên")
public class CreateAccountProvisioningRequest {

    @Schema(description = "ID nhân viên (nếu trong quy trình Onboarding có thể tự lấy theo Process)")
    private UUID employeeId;

    @NotBlank(message = "Tên hệ thống không được để trống")
    @Size(max = 100, message = "Tên hệ thống tối đa 100 ký tự")
    @Schema(description = "Tên hệ thống", example = "Google Workspace Email")
    private String systemName;

    @NotBlank(message = "Tên tài khoản/username không được để trống")
    @Size(max = 150, message = "Tên tài khoản tối đa 150 ký tự")
    @Schema(description = "Tên tài khoản / Email đăng nhập", example = "hieu.nv@cyclosa.com")
    private String accountUsername;

    @Schema(description = "Ghi chú tài khoản / mật khẩu tạm / thông số")
    private String notes;
}

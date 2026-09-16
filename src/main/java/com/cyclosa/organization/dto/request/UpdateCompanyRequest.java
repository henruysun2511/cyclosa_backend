package com.cyclosa.organization.dto.request;

import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Yêu cầu cập nhật thông tin công ty")
public class UpdateCompanyRequest {

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 200, message = "Tên công ty không vượt quá 200 ký tự")
    @Schema(description = "Tên đầy đủ của công ty", example = "Công ty Cổ phần Công nghệ CYCLOSA")
    private String name;

    @Size(max = 50, message = "Mã số thuế không vượt quá 50 ký tự")
    @Schema(description = "Mã số thuế", example = "0101234567")
    private String taxCode;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không vượt quá 100 ký tự")
    @Schema(description = "Email liên hệ chính", example = "contact@cyclosa.com")
    private String email;

    @Size(max = 20, message = "Số điện thoại không vượt quá 20 ký tự")
    @Schema(description = "Số điện thoại liên hệ", example = "02431234567")
    private String phone;

    @Schema(description = "Địa chỉ trụ sở chính", example = "Tầng 12, Tòa nhà Keangnam Landmark 72, Hà Nội")
    private String address;

    @Schema(description = "Đường dẫn Logo", example = "https://cdn.cyclosa.com/logos/main.png")
    private String logoUrl;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Schema(description = "Trạng thái hoạt động", example = "ACTIVE")
    private ActiveStatus status;
}

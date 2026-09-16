package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Yêu cầu tạo mới công ty / pháp nhân")
public class CreateCompanyRequest {

    @NotBlank(message = "Mã công ty không được để trống")
    @Size(max = 50, message = "Mã công ty không vượt quá 50 ký tự")
    @Schema(description = "Mã định danh công ty", example = "CYCLOSA_VN")
    private String code;

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
}

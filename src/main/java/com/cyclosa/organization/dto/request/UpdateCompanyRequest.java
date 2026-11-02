package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin công ty / pháp nhân")
public class UpdateCompanyRequest {

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 255, message = "Tên công ty tối đa 255 ký tự")
    @Schema(description = "Tên công ty / pháp nhân", example = "Công ty Cổ phần Tập đoàn CYCLOSA")
    private String name;

    @Size(max = 50, message = "Mã số thuế tối đa 50 ký tự")
    @Schema(description = "Mã số thuế doanh nghiệp", example = "0109998888")
    private String taxCode;

    @Size(max = 500, message = "Địa chỉ trụ sở tối đa 500 ký tự")
    @Schema(description = "Địa chỉ trụ sở chính", example = "Tòa nhà Keangnam Landmark 72, Nam Từ Liêm, Hà Nội")
    private String address;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Schema(description = "Số điện thoại liên hệ", example = "02473008888")
    private String phone;

    @Size(max = 100, message = "Email tối đa 100 ký tự")
    @Schema(description = "Email liên hệ chính thức", example = "contact@cyclosa.vn")
    private String email;

    @Size(max = 500, message = "Đường dẫn logo tối đa 500 ký tự")
    @Schema(description = "URL ảnh logo", example = "https://res.cloudinary.com/.../logo.png")
    private String logoUrl;

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Trạng thái chỉ nhận ACTIVE hoặc INACTIVE")
    @Schema(description = "Trạng thái hoạt động", example = "ACTIVE")
    private String status;
}


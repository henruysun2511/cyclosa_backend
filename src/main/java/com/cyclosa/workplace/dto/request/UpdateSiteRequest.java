package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSiteRequest {

    @NotNull(message = "Chi nhánh (branchId) không được để trống")
    private UUID branchId;

    @NotBlank(message = "Tên tòa nhà / cơ sở không được để trống")
    @Size(max = 150, message = "Tên tòa nhà không vượt quá 150 ký tự")
    private String name;

    @NotBlank(message = "Mã tòa nhà không được để trống")
    @Size(max = 50, message = "Mã tòa nhà không vượt quá 50 ký tự")
    private String code;

    @Size(max = 255, message = "Địa chỉ không vượt quá 255 ký tự")
    private String address;

    private String description;
}

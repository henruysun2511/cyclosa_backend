package com.cyclosa.role.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class RoleRequest {

    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 150, message = "Tên vai trò tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Mã vai trò không được để trống")
    @Pattern(regexp = "^[A-Z0-9_]{3,100}$", message = "Mã vai trò chỉ gồm chữ in hoa, số và dấu gạch dưới (3-100 ký tự)")
    private String code;

    private String description;

    private UUID companyId;
}

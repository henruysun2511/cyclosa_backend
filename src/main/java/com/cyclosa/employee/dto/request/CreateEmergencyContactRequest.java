package com.cyclosa.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmergencyContactRequest {

    @NotBlank(message = "Họ tên người liên hệ không được để trống")
    private String fullName;

    @NotBlank(message = "Mối quan hệ không được để trống")
    private String relationship;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String address;
}


package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.enums.FamilyRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEmergencyContactRequest {

    @NotBlank(message = "Họ tên người liên hệ khẩn cấp không được để trống")
    private String fullName;

    @NotNull(message = "Mối quan hệ không được để trống")
    private FamilyRelationship relationship;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String address;
}

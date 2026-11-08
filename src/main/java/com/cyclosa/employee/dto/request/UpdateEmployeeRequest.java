package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    @NotBlank(message = "Họ và tên đệm không được để trống")
    @Size(max = 100, message = "Họ và tên đệm tối đa 100 ký tự")
    private String lastName;

    @NotBlank(message = "Tên nhân viên không được để trống")
    @Size(max = 50, message = "Tên nhân viên tối đa 50 ký tự")
    private String firstName;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
    private String phone;

    private String personalEmail;
    private String address;
    private LocalDate startDate;
    private LocalDate probationEndDate;
    private String personalTaxCode;
    private String socialInsuranceNumber;
    private String portraitUrl;
}


package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.enums.Gender;
import com.cyclosa.employee.enums.MaritalStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdatePersonalInfoRequest {

    @NotBlank(message = "Họ và tên nhân viên không được để trống")
    private String fullName;

    private Gender gender;
    private LocalDate dateOfBirth;

    @NotBlank(message = "Số CCCD/Hộ chiếu không được để trống")
    private String nationalIdNumber;
    private LocalDate nationalIdIssueDate;
    private String nationalIdIssuePlace;

    private String taxCode;
    private String socialInsuranceNumber;

    private String bankAccountNumber;
    private String bankName;
    private String bankBranch;

    private MaritalStatus maritalStatus;
    private String nationality;
    private String personalEmail;
    private String phone;
    private String permanentAddress;
    private String currentAddress;
    private String photoUrl;
}

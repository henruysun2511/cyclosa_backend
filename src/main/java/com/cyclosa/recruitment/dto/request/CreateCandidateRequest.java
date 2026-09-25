package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.CandidateSource;
import com.cyclosa.recruitment.enums.CandidateStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCandidateRequest {
    private UUID companyId;
    @NotBlank(message = "Họ và tên ứng viên không được để trống")
    private String fullName;
    @NotBlank(message = "Email ứng viên không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    private String phone;
    private CandidateSource source;
    private CandidateStatus status;
    private String notes;
}

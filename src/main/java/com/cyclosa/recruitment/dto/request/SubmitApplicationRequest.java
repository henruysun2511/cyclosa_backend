package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitApplicationRequest {
    private UUID companyId;
    @NotNull(message = "Vị trí tuyển dụng không được để trống")
    private UUID jobPositionId;
    private UUID jobPostingId;
    private UUID candidateId;
    @NotBlank(message = "Họ tên ứng viên không được để trống")
    private String fullName;
    @NotBlank(message = "Email ứng viên không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    private String phone;
    @NotBlank(message = "Đường dẫn CV/Resume không được để trống")
    private String resumeUrl;
    private String coverLetter;
}

package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.IdentityDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIdentityDocumentRequest {

    @NotNull(message = "Loại giấy tờ không được để trống")
    private IdentityDocumentType documentType;

    @NotBlank(message = "Số giấy tờ không được để trống")
    private String documentNumber;

    @NotNull(message = "Ngày cấp không được để trống")
    private LocalDate issueDate;

    @NotBlank(message = "Nơi cấp không được để trống")
    private String issuePlace;

    private LocalDate expiryDate;
}


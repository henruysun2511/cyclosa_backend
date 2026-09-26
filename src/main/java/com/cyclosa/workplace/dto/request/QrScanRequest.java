package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrScanRequest {

    @NotBlank(message = "Dữ liệu mã QR không được để trống")
    private String qrCodeData;
}

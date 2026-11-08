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
public class UpdateBankAccountRequest {

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;

    private String bankBranchName;

    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;
}


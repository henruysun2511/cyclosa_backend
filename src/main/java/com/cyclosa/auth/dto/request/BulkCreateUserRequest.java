package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo hàng loạt tài khoản người dùng")
public class BulkCreateUserRequest {

    @NotEmpty(message = "Danh sách tài khoản không được để trống")
    @Valid
    @Schema(description = "Danh sách các tài khoản cần tạo")
    private List<BulkCreateUserItem> users;
}

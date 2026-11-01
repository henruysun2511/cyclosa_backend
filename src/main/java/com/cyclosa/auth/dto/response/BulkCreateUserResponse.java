package com.cyclosa.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả xử lý tạo hàng loạt tài khoản người dùng")
public class BulkCreateUserResponse {

    @Schema(description = "Tổng số tài khoản gửi lên yêu cầu", example = "10")
    private int totalRequested;

    @Schema(description = "Số tài khoản đã tạo thành công", example = "8")
    private int totalCreated;

    @Schema(description = "Số tài khoản bị bỏ qua do lỗi", example = "2")
    private int totalSkipped;

    @Schema(description = "Danh sách các tài khoản đã tạo thành công")
    private List<UserResponse> createdUsers;

    @Schema(description = "Danh sách các tài khoản bị bỏ qua kèm lý do")
    private List<BulkUserErrorItem> errors;
}


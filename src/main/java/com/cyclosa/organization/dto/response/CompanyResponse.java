package com.cyclosa.organization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết công ty / pháp nhân")
public class CompanyResponse {

    @Schema(description = "ID công ty", example = "00000000-0000-0000-0000-000000000001")
    private UUID id;

    @Schema(description = "Mã công ty", example = "CYC_CORP")
    private String code;

    @Schema(description = "Tên công ty", example = "Công ty Cổ phần Tập đoàn CYCLOSA")
    private String name;

    @Schema(description = "Mã số thuế", example = "0109998888")
    private String taxCode;

    @Schema(description = "Địa chỉ trụ sở chính")
    private String address;

    @Schema(description = "Số điện thoại liên hệ")
    private String phone;

    @Schema(description = "Email liên hệ chính thức")
    private String email;

    @Schema(description = "URL ảnh logo")
    private String logoUrl;

    @Schema(description = "Trạng thái hoạt động", example = "ACTIVE")
    private String status;

    @Schema(description = "Thời gian tạo bản ghi")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}


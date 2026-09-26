package com.cyclosa.onboarding.dto.request;

import com.cyclosa.onboarding.enums.EmployeeDocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tải lên tài liệu hồ sơ nhân viên")
public class UploadEmployeeDocumentRequest {

    @NotNull(message = "Loại tài liệu không được để trống")
    @Schema(description = "Loại tài liệu: ID_CARD, DEGREE, CERTIFICATE, CONTRACT, TAX_CODE, RESUME, OTHER")
    private EmployeeDocumentType documentType;

    @NotBlank(message = "Tên tài liệu không được để trống")
    @Size(max = 255, message = "Tên tài liệu tối đa 255 ký tự")
    @Schema(description = "Tên tài liệu", example = "Căn cước công dân gắn chip (Mặt trước + Mặt sau)")
    private String documentName;

    @NotBlank(message = "Đường dẫn file URL không được để trống")
    @Size(max = 500, message = "URL tối đa 500 ký tự")
    @Schema(description = "Đường dẫn file (Cloudinary hoặc S3)", example = "https://res.cloudinary.com/cyclosa/raw/upload/cccd.pdf")
    private String fileUrl;

    @Schema(description = "Kích thước file (bytes)")
    private Long fileSize;

    @Schema(description = "Ghi chú thêm về tài liệu")
    private String notes;
}

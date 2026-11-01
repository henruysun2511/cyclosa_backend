package com.cyclosa.media.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tệp tin đa phương tiện tải lên Cloudinary")
public class CloudinaryMediaResponse {

    @Schema(description = "Mã định danh duy nhất của ảnh trên Cloudinary", example = "cyclosa/avatars/an_nguyen_123456")
    private String publicId;

    @Schema(description = "Đường dẫn bảo mật HTTPS của ảnh", example = "https://res.cloudinary.com/cyclosa/image/upload/v123456/cyclosa/avatars/an_nguyen.jpg")
    private String secureUrl;

    @Schema(description = "Định dạng tệp tin", example = "jpg")
    private String format;

    @Schema(description = "Kích thước tệp tin (bytes)", example = "1048576")
    private Long bytes;

    @Schema(description = "Loại tài nguyên", example = "image")
    private String resourceType;
}


package com.cyclosa.media.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu xóa tệp tin đa phương tiện khỏi Cloudinary")
public class DeleteMediaRequest {

    @NotBlank(message = "Mã định danh publicId không được để trống")
    @Schema(description = "Mã định danh duy nhất của tệp tin trên Cloudinary", example = "cyclosa/avatars/an_nguyen_123456")
    private String publicId;
}

package com.cyclosa.media.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.media.dto.request.DeleteMediaRequest;
import com.cyclosa.media.dto.response.CloudinaryMediaResponse;
import com.cyclosa.media.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Media", description = "Các API tải lên và xóa tệp tin hình ảnh/đa phương tiện qua Cloudinary")
public class MediaController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải lên tệp tin hình ảnh lên Cloudinary", description = "Hỗ trợ định dạng .jpg, .jpeg, .png, .webp với dung lượng tối đa 5MB")
    public ApiResponse<CloudinaryMediaResponse> uploadImage(
            @Parameter(description = "Tệp tin hình ảnh cần tải lên", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Thư mục đích trên Cloudinary", example = "cyclosa/avatars")
            @RequestParam(value = "folder", required = false, defaultValue = "cyclosa/avatars") String folder
    ) {
        CloudinaryMediaResponse response = cloudinaryService.uploadImage(file, folder);
        return ApiResponse.ok(response, "Tải tệp tin lên thành công");
    }

    @DeleteMapping
    @Operation(summary = "Xóa tệp tin đa phương tiện khỏi Cloudinary", description = "Nhận mã định danh publicId qua query parameter hoặc JSON body để xóa ảnh an toàn")
    public ApiResponse<Void> deleteMedia(
            @Parameter(description = "Mã publicId của tệp tin cần xóa", example = "cyclosa/avatars/an_nguyen_123456")
            @RequestParam(value = "publicId", required = false) String publicIdParam,

            @Valid @RequestBody(required = false) DeleteMediaRequest requestBody
    ) {
        String publicId = (requestBody != null && requestBody.getPublicId() != null && !requestBody.getPublicId().isBlank())
                ? requestBody.getPublicId()
                : publicIdParam;

        cloudinaryService.deleteMedia(publicId);
        return ApiResponse.ok(null, "Xóa tệp tin thành công");
    }
}


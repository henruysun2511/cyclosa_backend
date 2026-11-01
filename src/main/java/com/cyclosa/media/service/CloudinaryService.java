package com.cyclosa.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.media.dto.response.CloudinaryMediaResponse;
import com.cyclosa.media.exception.MediaErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String DEFAULT_FOLDER = "cyclosa/general";
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Cloudinary cloudinary;

    public CloudinaryMediaResponse uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException(MediaErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(MediaErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(MediaErrorCode.INVALID_FILE_TYPE);
        }

        String targetFolder = (folder != null && !folder.isBlank()) ? folder.trim() : DEFAULT_FOLDER;
        log.info("Bắt đầu tải tệp tin [{}] lên Cloudinary folder: {}", file.getOriginalFilename(), targetFolder);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", targetFolder,
                            "resource_type", "image"
                    )
            );

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");
            Object bytesObj = uploadResult.get("bytes");
            Long bytes = bytesObj instanceof Number ? ((Number) bytesObj).longValue() : null;
            String resourceType = (String) uploadResult.get("resource_type");

            log.info("Tải tệp tin lên Cloudinary thành công: publicId={}, secureUrl={}", publicId, secureUrl);

            return CloudinaryMediaResponse.builder()
                    .publicId(publicId)
                    .secureUrl(secureUrl)
                    .format(format)
                    .bytes(bytes)
                    .resourceType(resourceType)
                    .build();

        } catch (IOException e) {
            log.error("Lỗi I/O khi tải tệp lên Cloudinary: {}", e.getMessage(), e);
            throw new AppException(MediaErrorCode.UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi tương tác với Cloudinary: {}", e.getMessage(), e);
            throw new AppException(MediaErrorCode.UPLOAD_FAILED);
        }
    }

    public void deleteMedia(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new AppException(MediaErrorCode.PUBLIC_ID_REQUIRED);
        }

        String cleanPublicId = publicId.trim();
        log.info("Yêu cầu xóa tệp tin Cloudinary: publicId={}", cleanPublicId);

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(cleanPublicId, ObjectUtils.emptyMap());
            String status = (String) result.get("result");

            log.info("Kết quả xóa Cloudinary cho publicId={}: result={}", cleanPublicId, status);

            if (!"ok".equalsIgnoreCase(status) && !"not found".equalsIgnoreCase(status)) {
                log.warn("Cloudinary phản hồi trạng thái không thành công: {}", status);
                throw new AppException(MediaErrorCode.DELETE_FAILED);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi xóa tệp tin trên Cloudinary: {}", e.getMessage(), e);
            throw new AppException(MediaErrorCode.DELETE_FAILED);
        }
    }
}

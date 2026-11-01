package com.cyclosa.media.service;

import com.cyclosa.media.dto.response.CloudinaryMediaResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryMediaResponse uploadImage(MultipartFile file, String folder);

    void deleteMedia(String publicId);
}

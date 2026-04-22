package com.example.backend.media.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2UploadService {

    private final S3Client s3;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-url}")
    private String publicBaseUrl;

    // =============================
    // CATEGORY IMAGE UPLOAD
    // =============================
    public String uploadCategoryImage(MultipartFile file) {
        return upload(file, "categories");
    }

    // =============================
    // TOOL LOGO UPLOAD ✅ NEW
    // =============================
    public String uploadToolLogo(MultipartFile file) {
        return upload(file, "tools");
    }

    // =============================
    // SHARED UPLOAD LOGIC
    // =============================
    private String upload(MultipartFile file, String folder) {

        validateImage(file);

        String key = folder + "/"
                + UUID.randomUUID()
                + getImageExtension(file);

        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }

        return key; // ✅ KEY ONLY
    }

    // =============================
    // DELETE
    // =============================
    public void delete(String key) {
        if (key == null || key.isBlank()) return;

        s3.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    // =============================
    // PUBLIC URL
    // =============================
    public String toPublicUrl(String key) {
        return key == null ? null : publicBaseUrl + "/" + key;
    }

    // =============================
    // HELPERS
    // =============================
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("File is empty");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new RuntimeException("Max image size is 5MB");

        String type = file.getContentType();
        if (type == null ||
                (!type.equals("image/jpeg")
                        && !type.equals("image/png")
                        && !type.equals("image/webp"))) {
            throw new RuntimeException("Only JPG / PNG / WEBP allowed");
        }
    }

    private String getImageExtension(MultipartFile file) {
        return switch (Objects.requireNonNull(file.getContentType())) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
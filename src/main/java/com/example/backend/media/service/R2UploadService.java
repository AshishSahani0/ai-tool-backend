package com.example.backend.media.service;

import com.example.backend.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2UploadService {

    private final S3Client s3;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/svg+xml",
            "image/gif"
    );

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
    // TOOL LOGO UPLOAD
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

        try (InputStream is = file.getInputStream()) {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .cacheControl("public, max-age=31536000, immutable")
                            .build(),
                    RequestBody.fromInputStream(is, file.getSize())
            );
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload image to R2 storage: {}", e.getMessage(), e);
            throw new BadRequestException("Image upload failed: " + e.getMessage());
        }

        return key;
    }

    // =============================
    // DELETE
    // =============================
    public void delete(String key) {
        if (key == null || key.isBlank()) return;

        try {
            s3.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        } catch (Exception e) {
            log.warn("Failed to delete object from R2: {}", key, e);
        }
    }

    // =============================
    // PUBLIC URL
    // =============================
    public String toPublicUrl(String key) {
        return key == null ? null : publicBaseUrl + "/" + key;
    }

    // =============================
    // HELPERS & SECURITY VALIDATION
    // =============================
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Maximum image size is 5MB");
        }

        String type = file.getContentType();
        if (type == null || !ALLOWED_TYPES.contains(type.toLowerCase())) {
            throw new BadRequestException("Unsupported image format. Allowed formats: JPG, PNG, WEBP, SVG, GIF");
        }

        // Validate genuine binary signature (magic bytes) to prevent MIME spoofing
        if (!isGenuineImage(file)) {
            throw new BadRequestException("Invalid file content: The uploaded file does not match a valid image signature");
        }
    }

    private boolean isGenuineImage(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[16];
            int read = is.read(header, 0, 16);
            if (read < 4) return false;

            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }

            // PNG: 89 50 4E 47 (0x89 'P' 'N' 'G')
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return true;
            }

            // GIF: 'G' 'I' 'F' '8'
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
                return true;
            }

            // WEBP: 'R' 'I' 'F' 'F' ... 'W' 'E' 'B' 'P'
            if (read >= 12 && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
                return true;
            }

            // SVG: text containing <svg or <?xml or <!DOCTYPE
            String headStr = new String(header, StandardCharsets.UTF_8).trim().toLowerCase();
            if (headStr.startsWith("<svg") || headStr.startsWith("<?xml") || headStr.startsWith("<!doc")) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.warn("Magic byte verification failed: {}", e.getMessage());
            return false;
        }
    }

    private String getImageExtension(MultipartFile file) {
        String type = file.getContentType();
        if (type == null) return ".jpg";
        return switch (type.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
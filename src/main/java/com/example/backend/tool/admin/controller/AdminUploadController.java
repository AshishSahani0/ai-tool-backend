package com.example.backend.tool.admin.controller;

import com.example.backend.tool.dto.UploadUrlResponse;
import com.example.backend.media.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
public class AdminUploadController {

    private final R2UploadService uploadService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/category-image")
    public UploadUrlResponse uploadCategoryImage(
            @RequestParam("file") MultipartFile file
    ) {
        String key = uploadService.uploadCategoryImage(file);

        return new UploadUrlResponse(
                null,
                uploadService.toPublicUrl(key)
        );
    }
}
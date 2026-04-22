package com.example.backend.media.controller;

import com.example.backend.tool.dto.UploadUrlResponse;
import com.example.backend.media.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user/media")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserMediaController {

    private final R2UploadService uploadService;

    @PostMapping("/tool-logo")
    public UploadUrlResponse uploadToolLogo(
            @RequestParam("file") MultipartFile file
    ) {
        String key = uploadService.uploadToolLogo(file); // 🔥 tools/

        return new UploadUrlResponse(
                key,
                uploadService.toPublicUrl(key)
        );
    }
}
package com.example.backend.user.controller;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.common.exception.UnauthorizedException;
import com.example.backend.user.dto.UpdateProfileRequest;
import com.example.backend.user.dto.UserProfileResponse;
import com.example.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserProfileResponse getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication is required to view profile");
        }
        return userService.getProfile(principal);
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication is required to view profile");
        }
        return userService.getProfile(principal);
    }

    @PutMapping("/profile")
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication is required to update profile");
        }
        return userService.updateProfile(principal, request);
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportUserData(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication is required to export data");
        }
        return ResponseEntity.ok(userService.exportUserData(principal));
    }
}


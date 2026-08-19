package com.example.backend.auth.controller;

import com.example.backend.auth.dto.MeResponse;
import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Full authentication is required to access /api/auth/me");
        }

        return MeResponse.builder()
                .id(principal.getId())
                .firebaseUid(principal.getUid())
                .email(principal.getEmail())
                .name(principal.getName())
                .role(principal.getRole())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletRequest request
    ) {
        String token = null;
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7).trim();
        }

        authService.logout(authentication, token);
        return ResponseEntity.ok().build();
    }
}
package com.example.backend.auth.controller;

import com.example.backend.auth.dto.MeResponse;
import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new RuntimeException("UNAUTHORIZED");
        }

        return new MeResponse(
                principal.getEmail(),
                principal.getRole()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication);
        return ResponseEntity.ok().build();
    }
}
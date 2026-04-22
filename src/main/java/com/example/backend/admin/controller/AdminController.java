package com.example.backend.admin.controller;

import com.example.backend.auth.dto.MeResponse;
import com.example.backend.auth.security.AuthPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public MeResponse me(Authentication authentication) {

        AuthPrincipal principal =
                (AuthPrincipal) authentication.getPrincipal();

        return new MeResponse(
                principal.getEmail(),
                principal.getRole()
        );
    }
}
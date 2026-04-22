package com.example.backend.user.controller;
import com.example.backend.auth.dto.MeResponse;
import com.example.backend.user.model.Role;
import com.example.backend.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @PostMapping("/export/pdf")
    public String exportPdf() {
        return "PDF Export Allowed for Logged-in Users";
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if (user.getRole() != Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return new MeResponse(user.getEmail(), user.getRole().name());
    }
}


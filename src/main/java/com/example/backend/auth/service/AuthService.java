package com.example.backend.auth.service;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.user.model.Role;
import com.example.backend.user.model.User;
import com.example.backend.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public AuthPrincipal validateUser(String uid, String email, String name) {

        User user = userRepository
                .findByFirebaseUid(uid)
                .orElseGet(() -> {
                    log.info("Creating new user for UID: {}", uid);

                    return userRepository.save(
                            User.builder()
                                    .firebaseUid(uid)
                                    .email(email)
                                    .name(name)
                                    .role(Role.USER)
                                    .build()
                    );
                });

        return new AuthPrincipal(
                user.getFirebaseUid(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public void logout(Authentication authentication) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            return;
        }

        try {
            FirebaseAuth.getInstance()
                    .revokeRefreshTokens(principal.getUid());
        } catch (Exception e) {
            log.warn("Failed to revoke Firebase tokens", e);
        }

        SecurityContextHolder.clearContext();
    }
}
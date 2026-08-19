package com.example.backend.auth.service;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.auth.security.FirebaseTokenCache;
import com.example.backend.auth.security.UserPrincipalCache;
import com.example.backend.user.model.Role;
import com.example.backend.user.model.User;
import com.example.backend.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserPrincipalCache userPrincipalCache;
    private final FirebaseTokenCache firebaseTokenCache;

    public AuthPrincipal validateUser(String uid, String email, String name) {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("User UID must not be null or blank");
        }

        // 1. Check in-memory UserPrincipalCache for O(1) retrieval (<0.05ms)
        AuthPrincipal cached = userPrincipalCache.get(uid);
        if (cached != null) {
            return cached;
        }

        // 2. Query MongoDB by indexed firebaseUid
        User user = userRepository
                .findByFirebaseUid(uid)
                .orElseGet(() -> {
                    log.info("Creating new user for UID: {}, Email: {}", uid, email);
                    try {
                        return userRepository.save(
                                User.builder()
                                        .firebaseUid(uid)
                                        .email(email != null ? email.toLowerCase().trim() : null)
                                        .name(name != null && !name.isBlank() ? name.trim() : "User")
                                        .role(Role.USER)
                                        .build()
                        );
                    } catch (DuplicateKeyException e) {
                        log.warn("Concurrent user creation detected for UID: {}. Fetching existing user.", uid);
                        return userRepository.findByFirebaseUid(uid)
                                .or(() -> email != null ? userRepository.findByEmail(email.toLowerCase().trim()) : java.util.Optional.empty())
                                .orElseThrow(() -> new IllegalStateException("Failed to resolve user after race condition", e));
                    }
                });

        // 3. Build enriched AuthPrincipal
        AuthPrincipal principal = AuthPrincipal.builder()
                .id(user.getId())
                .uid(user.getFirebaseUid())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole() != null ? user.getRole().name() : Role.USER.name())
                .build();

        // 4. Cache in memory for subsequent requests
        userPrincipalCache.put(uid, principal);

        return principal;
    }

    public void logout(Authentication authentication, String token) {
        if (token != null && !token.isBlank()) {
            firebaseTokenCache.evict(token);
        }

        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            userPrincipalCache.evict(principal.getUid());

            try {
                FirebaseAuth.getInstance().revokeRefreshTokens(principal.getUid());
                log.info("Successfully revoked Firebase refresh tokens for user: {}", principal.getUid());
            } catch (Exception e) {
                log.warn("Failed to revoke Firebase tokens for UID: {}", principal.getUid(), e);
            }
        }

        SecurityContextHolder.clearContext();
    }
}
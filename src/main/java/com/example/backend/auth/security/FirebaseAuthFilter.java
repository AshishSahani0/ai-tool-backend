package com.example.backend.auth.security;

import com.example.backend.auth.service.AuthService;
import com.example.backend.common.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final FirebaseTokenCache tokenCache;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            FirebaseToken decoded = tokenCache.get(token);

            if (decoded == null) {
                // Verify ID token and check revocation status
                decoded = FirebaseAuth.getInstance().verifyIdToken(token, true);
                tokenCache.put(token, decoded);
            }

            AuthPrincipal principal =
                    authService.validateUser(
                            decoded.getUid(),
                            decoded.getEmail(),
                            decoded.getName()
                    );

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            token,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + principal.getRole()
                                    )
                                    )
                            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {
            log.warn("Firebase token validation failed for path {}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();

            // If the path is public, do not reject unauthenticated/expired visitors; continue anonymously
            String uri = request.getRequestURI();
            if (uri != null && uri.startsWith("/api/public/")) {
                chain.doFilter(request, response);
                return;
            }

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiErrorResponse errorResponse = ApiErrorResponse.of(
                    HttpStatus.UNAUTHORIZED.value(),
                    "UNAUTHORIZED",
                    "Invalid, expired, or revoked authentication token",
                    uri
            );

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        chain.doFilter(request, response);
    }
}
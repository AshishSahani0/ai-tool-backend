package com.example.backend.config;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.common.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.*;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "rate.limit.enabled",
        havingValue = "true"
)
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;
    private final ObjectMapper objectMapper;

    // Anonymous users: 60 requests per minute
    private BucketConfiguration anonymousConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(
                        60,
                        Duration.ofMinutes(1)
                ))
                .build();
    }

    // Authenticated users: 300 requests per minute
    private BucketConfiguration userConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(
                        300,
                        Duration.ofMinutes(1)
                ))
                .build();
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof AuthPrincipal principal) {
            return "USER:" + principal.getUid();
        }

        return "IP:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
        }

        String clientIp = xfHeader.split(",")[0].trim();
        return clientIp.isEmpty() ? request.getRemoteAddr() : clientIp;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String key = resolveKey(request);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        Bucket bucket = proxyManager
                .builder()
                .build(
                        keyBytes,
                        () -> key.startsWith("USER:")
                                ? userConfig()
                                : anonymousConfig()
                );

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
            return;
        }

        log.warn("🚫 RATE LIMITED | key={} | path={}", key, request.getRequestURI());

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                429,
                "RATE_LIMIT_EXCEEDED",
                "Too many requests. Please slow down.",
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
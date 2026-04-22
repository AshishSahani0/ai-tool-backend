package com.example.backend.config;

import io.github.bucket4j.*;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    // Anonymous users
    private BucketConfiguration anonymousConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(
                        60,
                        Duration.ofMinutes(1)
                ))
                .build();
    }

    // Authenticated users
    private BucketConfiguration userConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(
                        300,
                        Duration.ofMinutes(1)
                ))
                .build();
    }

    private String resolveKey(HttpServletRequest request) {

        var auth = request.getUserPrincipal();

        if (auth != null) {
            return "USER:" + auth.getName();
        }

        return "IP:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0];
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String key = resolveKey(request);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // ✅ CORRECT METHOD
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
        response.setContentType("application/json");

        response.getWriter().write("""
                {
                  "error": "RATE_LIMIT_EXCEEDED",
                  "message": "Too many requests. Please slow down."
                }
                """);
    }
}
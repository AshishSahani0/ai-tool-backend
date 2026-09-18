package com.example.backend.auth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseTokenCache {

    private final Cache<String, FirebaseToken> cache =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(50_000)
                    .build();

    public FirebaseToken get(String token) {
        if (token == null) return null;
        String key = digest(token);
        FirebaseToken decoded = cache.getIfPresent(key);
        if (decoded != null) {
            // Ensure token expiration time is strictly in the future
            Object expVal = decoded.getClaims() != null ? decoded.getClaims().get("exp") : null;
            if (expVal instanceof Number expNumber) {
                long nowSeconds = System.currentTimeMillis() / 1000L;
                if (expNumber.longValue() <= nowSeconds) {
                    cache.invalidate(key);
                    return null;
                }
            }
        }
        return decoded;
    }

    public void put(String token, FirebaseToken decoded) {
        if (token != null && decoded != null) {
            cache.put(digest(token), decoded);
        }
    }

    public void evict(String token) {
        if (token != null) {
            cache.invalidate(digest(token));
        }
    }

    public void clear() {
        cache.invalidateAll();
    }

    private String digest(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(token.hashCode());
        }
    }
}
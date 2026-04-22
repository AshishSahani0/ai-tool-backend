package com.example.backend.auth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FirebaseTokenCache {

    private final Cache<String, FirebaseToken> cache =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(50_000)
                    .build();

    public FirebaseToken get(String token) {
        return cache.getIfPresent(token);
    }

    public void put(String token, FirebaseToken decoded) {
        cache.put(token, decoded);
    }
}
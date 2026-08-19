package com.example.backend.auth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserPrincipalCache {

    private final Cache<String, AuthPrincipal> cache =
            Caffeine.newBuilder()
                    .expireAfterWrite(15, TimeUnit.MINUTES)
                    .maximumSize(50_000)
                    .build();

    public AuthPrincipal get(String uid) {
        if (uid == null) return null;
        return cache.getIfPresent(uid);
    }

    public void put(String uid, AuthPrincipal principal) {
        if (uid != null && principal != null) {
            cache.put(uid, principal);
        }
    }

    public void evict(String uid) {
        if (uid != null) {
            cache.invalidate(uid);
        }
    }

    public void clear() {
        cache.invalidateAll();
    }
}

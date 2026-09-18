package com.example.backend.auth.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthPrincipal {

    private final String id;
    private final String uid;
    private final String email;
    private final String name;
    private final String role;
    private final boolean active;

    public AuthPrincipal(String uid, String email, String role) {
        this.id = null;
        this.uid = uid;
        this.email = email;
        this.name = null;
        this.role = role;
        this.active = true;
    }
}
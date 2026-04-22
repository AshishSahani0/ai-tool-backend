package com.example.backend.auth.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthPrincipal {

    private final String uid;
    private final String email;
    private final String role;
}
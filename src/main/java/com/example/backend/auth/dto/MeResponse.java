package com.example.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MeResponse {
    private final String id;
    private final String firebaseUid;
    private final String email;
    private final String name;
    private final String role;

    public MeResponse(String email, String role) {
        this.id = null;
        this.firebaseUid = null;
        this.email = email;
        this.name = null;
        this.role = role;
    }
}
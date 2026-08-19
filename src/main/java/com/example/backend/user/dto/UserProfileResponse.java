package com.example.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String id;
    private String firebaseUid;
    private String email;
    private String name;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;

    // Activity stats
    private long submittedToolsCount;
    private long approvedToolsCount;
    private long reviewsCount;
}

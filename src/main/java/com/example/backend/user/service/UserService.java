package com.example.backend.user.service;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.auth.security.UserPrincipalCache;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.exception.UnauthorizedException;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.review.model.ToolReview;
import com.example.backend.tool.review.repository.ToolReviewRepository;
import com.example.backend.user.dto.UpdateProfileRequest;
import com.example.backend.user.dto.UserProfileResponse;
import com.example.backend.user.model.Role;
import com.example.backend.user.model.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ToolRepository toolRepository;
    private final ToolReviewRepository toolReviewRepository;
    private final UserPrincipalCache userPrincipalCache;

    public UserProfileResponse getProfile(AuthPrincipal principal) {
        if (principal == null || principal.getUid() == null) {
            throw new UnauthorizedException("Authentication is required to retrieve profile");
        }

        User user = userRepository.findByFirebaseUid(principal.getUid())
                .orElseGet(() -> User.builder()
                        .firebaseUid(principal.getUid())
                        .email(principal.getEmail())
                        .name(principal.getName())
                        .role(Role.valueOf(principal.getRole()))
                        .build()
                );

        long submittedTools = toolRepository.countBySubmittedByUserId(principal.getUid());
        long approvedTools = toolRepository.countBySubmittedByUserIdAndApprovalStatus(
                principal.getUid(),
                ApprovalStatus.APPROVED
        );
        long reviewsCount = toolReviewRepository.countByUserId(principal.getUid());

        return UserProfileResponse.builder()
                .id(user.getId() != null ? user.getId() : principal.getId())
                .firebaseUid(principal.getUid())
                .email(user.getEmail() != null ? user.getEmail() : principal.getEmail())
                .name(user.getName() != null ? user.getName() : principal.getName())
                .role(user.getRole() != null ? user.getRole().name() : principal.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .submittedToolsCount(submittedTools)
                .approvedToolsCount(approvedTools)
                .reviewsCount(reviewsCount)
                .build();
    }

    public UserProfileResponse updateProfile(AuthPrincipal principal, UpdateProfileRequest request) {
        if (principal == null || principal.getUid() == null) {
            throw new UnauthorizedException("Authentication is required to update profile");
        }

        String updatedName = request.getName().trim();

        User user = userRepository.findByFirebaseUid(principal.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        user.setName(updatedName);
        user.setUpdatedAt(Instant.now());
        User saved = userRepository.save(user);

        // Update in-memory cache so subsequent token requests have updated name with 0 DB overhead
        AuthPrincipal updatedPrincipal = AuthPrincipal.builder()
                .id(saved.getId())
                .uid(saved.getFirebaseUid())
                .email(saved.getEmail())
                .name(saved.getName())
                .role(saved.getRole() != null ? saved.getRole().name() : Role.USER.name())
                .build();

        userPrincipalCache.put(principal.getUid(), updatedPrincipal);

        long submittedTools = toolRepository.countBySubmittedByUserId(principal.getUid());
        long approvedTools = toolRepository.countBySubmittedByUserIdAndApprovalStatus(
                principal.getUid(),
                ApprovalStatus.APPROVED
        );
        long reviewsCount = toolReviewRepository.countByUserId(principal.getUid());

        return UserProfileResponse.builder()
                .id(saved.getId())
                .firebaseUid(saved.getFirebaseUid())
                .email(saved.getEmail())
                .name(saved.getName())
                .role(saved.getRole() != null ? saved.getRole().name() : Role.USER.name())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .submittedToolsCount(submittedTools)
                .approvedToolsCount(approvedTools)
                .reviewsCount(reviewsCount)
                .build();
    }

    public Map<String, Object> exportUserData(AuthPrincipal principal) {
        if (principal == null || principal.getUid() == null) {
            throw new UnauthorizedException("Authentication is required to export data");
        }

        User user = userRepository.findByFirebaseUid(principal.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        List<Tool> submittedTools = toolRepository
                .findBySubmittedByUserIdOrderByCreatedAtDesc(principal.getUid(), PageRequest.of(0, 500))
                .getContent();

        Map<String, Object> export = new LinkedHashMap<>();
        export.put("exportDate", Instant.now().toString());
        export.put("profile", Map.of(
                "id", user.getId() != null ? user.getId() : "",
                "firebaseUid", user.getFirebaseUid(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "name", user.getName() != null ? user.getName() : "",
                "role", user.getRole() != null ? user.getRole().name() : "USER",
                "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
        ));
        export.put("submittedToolsCount", submittedTools.size());
        export.put("submittedTools", submittedTools);

        return export;
    }
}

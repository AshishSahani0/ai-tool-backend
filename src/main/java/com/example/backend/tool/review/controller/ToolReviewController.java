package com.example.backend.tool.review.controller;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.tool.dto.ReviewRequest;
import com.example.backend.tool.review.model.ToolReview;
import com.example.backend.tool.review.service.ToolReviewService;
import com.example.backend.user.model.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/tools")
public class ToolReviewController {

    private final ToolReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping("/{toolId}/reviews")
    public void addReview(
            @PathVariable String toolId,
            @RequestBody ReviewRequest request,
            Authentication authentication
    ) {

        String userId = null;
        String firebaseName = null;

        if (authentication != null &&
                authentication.getPrincipal() instanceof AuthPrincipal principal) {

            userId = principal.getUid();

            // 🔥 Get REAL name from DB
            User user = userRepository
                    .findByFirebaseUid(userId)
                    .orElse(null);

            if (user != null) {
                firebaseName = user.getName();
            }
        }

        reviewService.addReview(toolId, request, userId, firebaseName);
    }

    @GetMapping("/{toolId}/reviews")
    public List<ToolReview> getReviews(
            @PathVariable String toolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reviewService
                .getReviews(toolId, page, size)
                .getContent();
    }
}
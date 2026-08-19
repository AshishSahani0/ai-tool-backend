package com.example.backend.tool.review.controller;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.tool.dto.ReviewRequest;
import com.example.backend.tool.dto.ReviewResponse;
import com.example.backend.tool.review.service.ToolReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/tools")
public class ToolReviewController {

    private final ToolReviewService reviewService;

    @PostMapping("/{toolId}/reviews")
    public void addReview(
            @PathVariable String toolId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        String userId = null;
        String userName = null;

        if (authentication != null &&
                authentication.getPrincipal() instanceof AuthPrincipal principal) {
            userId = principal.getUid();
            userName = principal.getName() != null && !principal.getName().isBlank()
                    ? principal.getName()
                    : principal.getEmail();
        }

        reviewService.addReview(toolId, request, userId, userName);
    }

    @GetMapping("/{toolId}/reviews")
    public List<ReviewResponse> getReviews(
            @PathVariable String toolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reviewService
                .getReviews(toolId, page, size)
                .getContent();
    }
}
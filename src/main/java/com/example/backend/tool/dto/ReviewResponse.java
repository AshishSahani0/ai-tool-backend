package com.example.backend.tool.dto;

import com.example.backend.tool.review.model.ToolReview;

import java.time.Instant;

public record ReviewResponse(
        String id,
        String name,
        int rating,
        String comment,
        Instant createdAt
) {
    public static ReviewResponse fromEntity(ToolReview review) {
        if (review == null) return null;
        return new ReviewResponse(
                review.getId(),
                review.getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}

package com.example.backend.tool.dto;

import com.example.backend.tool.review.model.ToolReview;

import java.io.Serializable;
import java.time.Instant;

public record ReviewResponse(
        String id,
        String name,
        int rating,
        String comment,
        Instant createdAt
) implements Serializable {
    private static final long serialVersionUID = 1L;

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


package com.example.backend.tool.review.service;

import com.example.backend.common.exception.BadRequestException;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.tool.dto.ReviewRequest;
import com.example.backend.tool.review.model.ToolReview;
import com.example.backend.tool.review.repository.ToolReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ToolReviewService {

    private final ToolReviewRepository reviewRepo;
    private final ToolRepository toolRepository;

    public void addReview(
            String toolId,
            ReviewRequest request,
            String userId,
            String firebaseName
    ) {

        if (request.rating() < 1 || request.rating() > 5) {
            throw new BadRequestException("Invalid rating");
        }

        if (request.comment() == null ||
                request.comment().trim().length() < 5) {
            throw new BadRequestException("Comment too short");
        }

        // Prevent duplicate review from same logged-in user
        if (userId != null &&
                reviewRepo.existsByToolIdAndUserId(toolId, userId)) {
            throw new BadRequestException(
                    "You already reviewed this tool"
            );
        }

        // ✅ Correct name logic
        String finalName =
                userId != null
                        ? firebaseName
                        : request.name();

        if (finalName == null || finalName.trim().isBlank()) {
            throw new BadRequestException("Name required");
        }

        ToolReview review = ToolReview.builder()
                .toolId(toolId)
                .userId(userId) // null for guests
                .name(finalName.trim())
                .rating(request.rating())
                .comment(request.comment().trim())
                .createdAt(Instant.now())
                .build();

        reviewRepo.save(review);

        // Update tool rating
        toolRepository.updateRating(toolId, request.rating());
    }

    public Page<ToolReview> getReviews(
            String toolId,
            int page,
            int size
    ) {
        return reviewRepo.findByToolId(
                toolId,
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }
}
package com.example.backend.tool.review.repository;

import com.example.backend.tool.review.model.ToolReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ToolReviewRepository extends MongoRepository<ToolReview, String> {

    Page<ToolReview> findByToolId(String toolId, Pageable pageable);

    boolean existsByToolIdAndUserId(String toolId, String userId);
}
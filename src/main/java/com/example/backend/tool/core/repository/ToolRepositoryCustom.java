package com.example.backend.tool.core.repository;

import com.example.backend.tool.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ToolRepositoryCustom {

    void incrementViews(String toolId);

    void updateRating(String toolId, int newRating);
    Page<ToolCardProjection> findPublicTools(
            String subCategoryId,
            String pricingType,
            Boolean verified,
            ApprovalStatus status,
            Pageable pageable
    );


}
package com.example.backend.tool.core.repository;

import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.enums.PricingType;
import com.example.backend.tool.core.model.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ToolRepository
        extends MongoRepository<Tool, String>, ToolRepositoryCustom {

    /* =========================
       🔹 PUBLIC QUERIES
       ========================= */

    Page<ToolCardProjection> findByApprovalStatusAndActiveTrue(
            ApprovalStatus status,
            Pageable pageable
    );

    Page<ToolCardProjection> findByCategoryIdAndApprovalStatusAndActiveTrue(
            String categoryId,
            ApprovalStatus status,
            Pageable pageable
    );

    Page<ToolCardProjection> findBySubCategoryIdAndApprovalStatusAndActiveTrue(
            String subCategoryId,
            ApprovalStatus status,
            Pageable pageable
    );

    Page<ToolCardProjection> findByPricingTypeAndApprovalStatusAndActiveTrue(
            PricingType pricingType,
            ApprovalStatus status,
            Pageable pageable
    );

    /* =========================
       🔹 HOMEPAGE SECTIONS
       ========================= */

    Page<ToolCardProjection> findByFeaturedTrueAndApprovalStatusAndActiveTrue(
            ApprovalStatus status,
            Pageable pageable
    );

    Page<ToolCardProjection> findByApprovalStatusAndActiveTrueOrderByRatingDesc(
            ApprovalStatus status,
            Pageable pageable
    );

    Page<ToolCardProjection> findByApprovalStatusAndActiveTrueOrderByViewsDesc(
            ApprovalStatus status,
            Pageable pageable
    );

    Page<ToolCardProjection> findByApprovalStatusAndActiveTrueOrderByPopularityScoreDesc(
            ApprovalStatus status,
            Pageable pageable
    );

    /* =========================
       🔹 TEXT SEARCH
       ========================= */


    /* =========================
       🔹 USER QUERIES
       ========================= */

    Page<Tool> findBySubmittedByUserIdOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );

    Optional<Tool> findByIdAndSubmittedByUserId(
            String id,
            String userId
    );

    /* =========================
       🔹 ADMIN / MODERATION
       ========================= */

    Page<Tool> findByApprovalStatus(
            ApprovalStatus status,
            Pageable pageable
    );

    Page<Tool> findByActiveFalse(
            Pageable pageable
    );

    /* =========================
       🔹 COMPARE
       ========================= */

    List<Tool> findByIdInAndApprovalStatusAndActiveTrue(
            List<String> ids,
            ApprovalStatus status
    );

    /* =========================
       🔹 ANALYTICS
       ========================= */

    long countByApprovalStatusAndActiveTrue(
            ApprovalStatus status
    );

    long countByCategoryIdAndApprovalStatusAndActiveTrue(
            String categoryId,
            ApprovalStatus status
    );

    long countBySubCategoryIdAndApprovalStatusAndActiveTrue(
            String subCategoryId,
            ApprovalStatus status
    );

    /* =========================
       🔹 UTILITY
       ========================= */

    boolean existsBySlug(String slug);

    Optional<Tool> findBySlugAndApprovalStatusAndActiveTrue(
            String slug,
            ApprovalStatus status
    );

    List<Tool> findByActiveTrueAndApprovalStatus(String approvalStatus);
}
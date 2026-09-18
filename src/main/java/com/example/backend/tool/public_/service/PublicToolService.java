package com.example.backend.tool.public_.service;

import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolCardProjection;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.tool.dto.ToolCardResponse;
import com.example.backend.tool.dto.ToolResponse;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.enums.PricingType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PublicToolService {

    private final ToolRepository repo;
    private final MongoTemplate mongoTemplate;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("views", "rating", "reviewsCount", "popularityScore", "createdAt");

    /* =====================================
       LIST APPROVED TOOLS (Paginated)
       ===================================== */
    public Page<ToolCardResponse> listApprovedTools(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "popularityScore")
        );

        return repo.findByApprovalStatusAndActiveTrue(
                        ApprovalStatus.APPROVED,
                        pageable
                )
                .map(this::mapProjectionToCard);
    }

    /* =====================================
       GET TOOL BY SLUG (Public Safe, Cached)
       ===================================== */
    @Cacheable(value = "tool_by_slug", key = "#slug")
    public ToolResponse getBySlug(String slug) {
        Tool tool = repo.findBySlugAndApprovalStatusAndActiveTrue(
                        slug,
                        ApprovalStatus.APPROVED
                )
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with slug: " + slug));

        return mapEntityToResponse(tool);
    }

    /* =====================================
       RECORD VIEW ASYNCHRONOUSLY
       ===================================== */
    public void recordViewAsync(String toolId) {
        if (toolId != null && !toolId.isBlank()) {
            CompletableFuture.runAsync(() -> repo.incrementViews(toolId));
        }
    }

    /* =====================================
       GET RELATED / ALTERNATIVE TOOLS (Cached)
       ===================================== */
    @Cacheable(value = "tools_related", key = "#slug + '_' + #limit")
    public List<ToolCardResponse> getRelatedTools(String slug, int limit) {
        Tool tool = repo.findBySlugAndApprovalStatusAndActiveTrue(
                slug,
                ApprovalStatus.APPROVED
        ).orElse(null);

        if (tool == null || tool.getSubCategoryId() == null) {
            return List.of();
        }

        int safeLimit = Math.min(Math.max(limit, 1), 12);
        Pageable pageable = PageRequest.of(
                0,
                safeLimit + 1,
                Sort.by(Sort.Direction.DESC, "popularityScore")
        );

        return repo.findBySubCategoryIdAndApprovalStatusAndActiveTrue(
                        tool.getSubCategoryId(),
                        ApprovalStatus.APPROVED,
                        pageable
                )
                .stream()
                .filter(t -> !t.getSlug().equalsIgnoreCase(slug))
                .limit(safeLimit)
                .map(this::mapProjectionToCard)
                .toList();
    }

    /* =====================================
       FILTER TOOLS (Dynamic Query with Field Projections)
       ===================================== */
    public Page<ToolCardResponse> filterTools(
            String subCategoryId,
            String pricingType,
            Boolean verified,
            String sortBy,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);

        Criteria criteria = Criteria.where("approvalStatus").is(ApprovalStatus.APPROVED)
                .and("active").is(true);

        if (subCategoryId != null && !subCategoryId.isBlank()) {
            criteria.and("subCategoryId").is(subCategoryId);
        }

        if (pricingType != null && !pricingType.isBlank()) {
            try {
                criteria.and("pricingType").is(PricingType.valueOf(pricingType.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid pricing type: " + pricingType);
            }
        }

        if (verified != null) {
            criteria.and("verified").is(verified);
        }

        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "popularityScore";
        }

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, sortBy)
        );

        // Include only card fields to reduce MongoDB payload & deserialization cost
        Query query = new Query(criteria).with(pageable);
        query.fields()
                .include("slug")
                .include("name")
                .include("shortDescription")
                .include("logoKey")
                .include("pricingType")
                .include("rating")
                .include("reviewsCount")
                .include("views")
                .include("verified")
                .include("website")
                .include("hashtags");

        List<Tool> tools = mongoTemplate.find(query, Tool.class);
        long total = mongoTemplate.count(new Query(criteria), Tool.class);

        return new PageImpl<>(
                tools.stream()
                        .map(this::mapEntityToCard)
                        .toList(),
                pageable,
                total
        );
    }

    /* =====================================
       MAPPERS
       ===================================== */

    // Projection → Card DTO
    private ToolCardResponse mapProjectionToCard(ToolCardProjection p) {
        return new ToolCardResponse(
                p.getSlug(),
                p.getName(),
                p.getShortDescription(),
                p.getLogoKey(),
                p.getPricingType(),
                p.getRating(),
                p.getReviewsCount(),
                p.getViews(),
                p.isVerified(),
                p.getWebsite(),
                p.getHashtags()
        );
    }

    // Entity → Card DTO
    private ToolCardResponse mapEntityToCard(Tool tool) {
        return new ToolCardResponse(
                tool.getSlug(),
                tool.getName(),
                tool.getShortDescription(),
                tool.getLogoKey(),
                tool.getPricingType(),
                tool.getRating(),
                tool.getReviewsCount(),
                tool.getViews(),
                tool.isVerified(),
                tool.getWebsite(),
                tool.getHashtags()
        );
    }

    // Entity → Full DTO
    private ToolResponse mapEntityToResponse(Tool tool) {
        return new ToolResponse(
                tool.getId(),
                tool.getSlug(),
                tool.getName(),
                tool.getWebsite(),
                tool.getShortDescription(),
                tool.getLongDescription(),
                tool.getDifferentiation(),
                tool.getLogoKey(),
                tool.getHashtags(),
                tool.getPricingType(),
                tool.getPricingDetails(),
                tool.getPros(),
                tool.getCons(),
                tool.getUseCases(),
                tool.getUniqueFeatures(),
                tool.getRating(),
                tool.getReviewsCount(),
                tool.getViews(),
                tool.isVerified()
        );
    }
}
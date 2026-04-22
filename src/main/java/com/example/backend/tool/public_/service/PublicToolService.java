package com.example.backend.tool.public_.service;

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
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "popularityScore")
        );

        return repo.findByApprovalStatusAndActiveTrue(
                        ApprovalStatus.APPROVED,
                        pageable
                )
                .map(this::mapProjectionToCard);
    }

    /* =====================================
       GET TOOL BY SLUG (Public Safe)
       ===================================== */
    public ToolResponse getBySlug(String slug) {

        Tool tool = repo.findBySlugAndApprovalStatusAndActiveTrue(
                        slug,
                        ApprovalStatus.APPROVED
                )
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        repo.incrementViews(tool.getId());

        return mapEntityToResponse(tool);
    }

    /* =====================================
       FILTER TOOLS (Dynamic Query)
       ===================================== */
    public Page<ToolCardResponse> filterTools(
            String subCategoryId,
            String pricingType,
            Boolean verified,
            String sortBy,
            int page,
            int size
    ) {

        Query query = new Query();

        query.addCriteria(Criteria.where("approvalStatus")
                .is(ApprovalStatus.APPROVED));
        query.addCriteria(Criteria.where("active").is(true));

        if (subCategoryId != null) {
            query.addCriteria(Criteria.where("subCategoryId").is(subCategoryId));
        }

        if (pricingType != null) {
            try {
                query.addCriteria(Criteria.where("pricingType")
                        .is(PricingType.valueOf(pricingType.toUpperCase())));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid pricing type");
            }
        }

        if (verified != null) {
            query.addCriteria(Criteria.where("verified").is(verified));
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "popularityScore";
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortBy)
        );

        query.with(pageable);

        List<Tool> tools = mongoTemplate.find(query, Tool.class);

        Query countQuery = new Query((CriteriaDefinition) query.getQueryObject());
        long total = mongoTemplate.count(countQuery, Tool.class);

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
                p.isVerified()
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
                tool.isVerified()
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
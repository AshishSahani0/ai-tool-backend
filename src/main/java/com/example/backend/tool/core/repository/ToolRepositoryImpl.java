package com.example.backend.tool.core.repository;

import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ToolRepositoryImpl implements ToolRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public void incrementViews(String toolId) {

        Query query = new Query(Criteria.where("_id").is(toolId));

        Update update = new Update()
                .inc("views", 1)
                .set("updatedAt", Instant.now());

        mongoTemplate.updateFirst(query, update, Tool.class);
    }

    @Override
    public void updateRating(String toolId, int newRating) {

        Query query = new Query(Criteria.where("_id").is(toolId));

        Update update = new Update()
                .inc("ratingSum", newRating)
                .inc("reviewsCount", 1)
                .set("updatedAt", Instant.now());

        mongoTemplate.updateFirst(query, update, Tool.class);

        Tool tool = mongoTemplate.findById(toolId, Tool.class);
        if (tool == null) return;

        double avgRating = tool.getReviewsCount() == 0
                ? 0
                : (double) tool.getRatingSum() / tool.getReviewsCount();

        double popularityScore =
                (avgRating * Math.log(tool.getReviewsCount() + 1))
                        + (tool.getViews() * 0.1);

        Update finalUpdate = new Update()
                .set("rating", avgRating)
                .set("popularityScore", popularityScore)
                .set("updatedAt", Instant.now());

        mongoTemplate.updateFirst(query, finalUpdate, Tool.class);
    }

    @Override
    public Page<ToolCardProjection> findPublicTools(
            String subCategoryId,
            String pricingType,
            Boolean verified,
            ApprovalStatus status,
            Pageable pageable
    ) {

        Criteria criteria = Criteria.where("subCategoryId").is(subCategoryId)
                .and("approvalStatus").is(status)
                .and("active").is(true);

        if (pricingType != null && !pricingType.isBlank()) {
            criteria.and("pricingType").is(pricingType);
        }

        if (verified != null) {
            criteria.and("verified").is(verified);
        }

        Query query = new Query(criteria).with(pageable);

        long total = mongoTemplate.count(query, Tool.class);
        List<Tool> tools = mongoTemplate.find(query, Tool.class);

        List<ToolCardProjection> projections = tools.stream()
                .map(tool -> new ToolCardProjection() {
                    @Override public String getSlug() { return tool.getSlug(); }
                    @Override public String getName() { return tool.getName(); }
                    @Override public String getShortDescription() { return tool.getShortDescription(); }
                    @Override public String getLogoKey() { return tool.getLogoKey(); }
                    @Override public com.example.backend.tool.enums.PricingType getPricingType() { return tool.getPricingType(); }
                    @Override public double getRating() { return tool.getRating(); }
                    @Override public int getReviewsCount() { return tool.getReviewsCount(); }
                    @Override public int getViews() { return tool.getViews(); }
                    @Override public boolean isVerified() { return tool.isVerified(); }
                    @Override public String getWebsite() { return tool.getWebsite(); }
                    @Override public List<String> getHashtags() { return tool.getHashtags(); }
                })
                .collect(java.util.stream.Collectors.toList());

        return new PageImpl<>(projections, pageable, total);
    }
}
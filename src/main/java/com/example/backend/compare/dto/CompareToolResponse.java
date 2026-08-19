package com.example.backend.compare.dto;

import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.enums.PricingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareToolResponse {

    private String id;
    private String slug;
    private String name;
    private String shortDescription;
    private String logoKey;
    private PricingType pricingType;
    private String pricingDetails;
    private double rating;
    private int reviewsCount;
    private long views;
    private double popularityScore;
    private boolean verified;
    private String website;
    private List<String> hashtags;
    private List<String> uniqueFeatures;
    private List<String> pros;
    private List<String> cons;
    private List<String> useCases;
    private Instant updatedAt;

    public static CompareToolResponse fromEntity(Tool tool) {
        if (tool == null) return null;

        return CompareToolResponse.builder()
                .id(tool.getId())
                .slug(tool.getSlug())
                .name(tool.getName())
                .shortDescription(tool.getShortDescription())
                .logoKey(tool.getLogoKey())
                .pricingType(tool.getPricingType())
                .pricingDetails(tool.getPricingDetails())
                .rating(tool.getRating())
                .reviewsCount(tool.getReviewsCount())
                .views(tool.getViews())
                .popularityScore(tool.getPopularityScore())
                .verified(tool.isVerified())
                .website(tool.getWebsite())
                .hashtags(tool.getHashtags() != null ? tool.getHashtags() : List.of())
                .uniqueFeatures(tool.getUniqueFeatures() != null ? tool.getUniqueFeatures() : List.of())
                .pros(tool.getPros() != null ? tool.getPros() : List.of())
                .cons(tool.getCons() != null ? tool.getCons() : List.of())
                .useCases(tool.getUseCases() != null ? tool.getUseCases() : List.of())
                .updatedAt(tool.getUpdatedAt() != null ? tool.getUpdatedAt() : tool.getCreatedAt())
                .build();
    }
}

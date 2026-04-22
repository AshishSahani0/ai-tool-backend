package com.example.backend.tool.dto;

import com.example.backend.tool.enums.PricingType;

import java.util.List;

public record ToolResponse(
        String id,
        String slug,
        String name,
        String website,
        String shortDescription,
        String longDescription,
        String differentiation,
        String logoKey,
        List<String> hashtags,
        PricingType pricingType,
        String pricingDetails,
        List<String> pros,
        List<String> cons,
        List<String> useCases,
        List<String> uniqueFeatures,
        double rating,
        int reviewsCount,
        int views,
        boolean verified
) {}

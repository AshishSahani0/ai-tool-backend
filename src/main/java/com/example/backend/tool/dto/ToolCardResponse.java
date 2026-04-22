package com.example.backend.tool.dto;

import com.example.backend.tool.enums.PricingType;

public record ToolCardResponse(
        String slug,
        String name,
        String shortDescription,
        String logoKey,
        PricingType pricingType,
        double rating,
        int reviewsCount,
        int views,
        boolean verified
) {}
package com.example.backend.tool.dto;

import com.example.backend.tool.enums.PricingType;

import java.io.Serializable;
import java.util.List;

public record ToolCardResponse(
        String slug,
        String name,
        String shortDescription,
        String logoKey,
        PricingType pricingType,
        double rating,
        int reviewsCount,
        int views,
        boolean verified,
        String website,
        List<String> hashtags
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
package com.example.backend.tool.user.dto;

import com.example.backend.tool.enums.PricingType;

import java.util.List;

public record ToolCreateRequest(

        /* BASIC */
        String name,
        String website,
        String shortDescription,
        String longDescription,
        String differentiation,

        /* MEDIA */
        String logoKey,

        /* CATEGORY */
        String categoryId,
        String subCategoryId,
        List<String> hashtags,

        /* PRICING */
        PricingType pricingType,
        String pricingDetails,

        /* ADVANCED CONTENT */
        List<String> pros,
        List<String> cons,
        List<String> useCases,
        List<String> uniqueFeatures

) {}
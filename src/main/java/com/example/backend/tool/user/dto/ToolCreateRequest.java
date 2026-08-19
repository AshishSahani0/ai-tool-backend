package com.example.backend.tool.user.dto;

import com.example.backend.tool.enums.PricingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ToolCreateRequest(

        /* BASIC */
        @NotBlank(message = "Tool name is required")
        @Size(max = 100, message = "Tool name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Website URL is required")
        @Size(max = 500, message = "Website URL is too long")
        String website,

        @NotBlank(message = "Short description is required")
        @Size(max = 300, message = "Short description cannot exceed 300 characters")
        String shortDescription,

        @Size(max = 5000, message = "Long description cannot exceed 5000 characters")
        String longDescription,

        @Size(max = 2000, message = "Differentiation description cannot exceed 2000 characters")
        String differentiation,

        /* MEDIA */
        String logoKey,

        /* CATEGORY */
        @NotBlank(message = "Category is required")
        String categoryId,

        @NotBlank(message = "Subcategory is required")
        String subCategoryId,

        List<String> hashtags,

        /* PRICING */
        @NotNull(message = "Pricing type is required")
        PricingType pricingType,

        @Size(max = 1000, message = "Pricing details cannot exceed 1000 characters")
        String pricingDetails,

        /* ADVANCED CONTENT */
        List<String> pros,
        List<String> cons,
        List<String> useCases,
        List<String> uniqueFeatures

) {}
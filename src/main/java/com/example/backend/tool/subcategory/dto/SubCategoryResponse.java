package com.example.backend.tool.subcategory.dto;

public record SubCategoryResponse(
        String id,
        String name,
        String slug,
        int order
) {}
package com.example.backend.tool.subcategory.dto;

public record SubCategoryRequest(
        String categoryId,
        String name,
        int order
) {}

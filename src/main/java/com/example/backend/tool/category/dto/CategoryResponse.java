package com.example.backend.tool.category.dto;

public record CategoryResponse(
        String id,
        String name,
        String slug,
        String imageKey,
        int order
) {}
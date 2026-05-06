package com.example.backend.tool.category.dto;

import com.example.backend.tool.dto.SubCategoryWithCount;

import java.util.List;

public record CategoryWithSubsResponse(
        String id,
        String name,
        String imageKey,
        List<SubCategoryWithCount> subCategories
) {
}
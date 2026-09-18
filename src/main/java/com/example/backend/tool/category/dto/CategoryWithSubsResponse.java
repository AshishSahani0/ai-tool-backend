package com.example.backend.tool.category.dto;

import com.example.backend.tool.dto.SubCategoryWithCount;

import java.io.Serializable;
import java.util.List;

public record CategoryWithSubsResponse(
        String id,
        String name,
        String imageKey,
        List<SubCategoryWithCount> subCategories
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
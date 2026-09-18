package com.example.backend.tool.subcategory.dto;

import java.io.Serializable;

public record SubCategoryResponse(
        String id,
        String name,
        String slug,
        int order
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
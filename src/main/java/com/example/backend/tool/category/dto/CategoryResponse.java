package com.example.backend.tool.category.dto;

import java.io.Serializable;

public record CategoryResponse(
        String id,
        String name,
        String slug,
        String imageKey,
        int order
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
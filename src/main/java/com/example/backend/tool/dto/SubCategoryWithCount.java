package com.example.backend.tool.dto;

import java.io.Serializable;

public record SubCategoryWithCount(
        String id,
        String name,
        long toolCount
) implements Serializable {
    private static final long serialVersionUID = 1L;
}


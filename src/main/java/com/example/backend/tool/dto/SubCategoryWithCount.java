package com.example.backend.tool.dto;


public record SubCategoryWithCount(
        String id,
        String name,
        long toolCount
) {}

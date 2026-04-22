package com.example.backend.tool.dto;

public record CategoryRequest(
        String name,
        String imageKey,
        int order
) {}

package com.example.backend.tool.dto;

public record ReviewRequest(
        String name,
        int rating,
        String comment
) {}

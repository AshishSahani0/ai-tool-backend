package com.example.backend.tool.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        int rating,

        @NotBlank(message = "Review comment is required")
        @Size(min = 5, max = 1000, message = "Comment must be between 5 and 1000 characters")
        String comment
) {}

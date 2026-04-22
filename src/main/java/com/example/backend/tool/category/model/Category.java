package com.example.backend.tool.category.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "categories")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Category {

    @Id
    private String id;

    private String name;        // AI Productivity Tools
    private String slug;        // ai-productivity-tools
    private String imageKey;   // uploaded image
    private int order;
    private boolean active;

    private Instant updatedAt;
    private Instant createdAt;
}
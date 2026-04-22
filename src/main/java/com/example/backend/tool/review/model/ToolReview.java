package com.example.backend.tool.review.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("tool_reviews")
@CompoundIndex(
        name = "tool_user_unique_idx",
        def = "{'toolId':1,'userId':1}",
        unique = true
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolReview {

    @Id
    private String id;

    private String toolId;
    private String userId;
    private String name;

    private int rating;
    private String comment;

    private Instant createdAt;
}
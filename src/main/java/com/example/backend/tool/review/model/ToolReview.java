package com.example.backend.tool.review.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("tool_reviews")
@CompoundIndexes({
        @CompoundIndex(
                name = "tool_user_unique_idx",
                def = "{'toolId':1,'userId':1}",
                unique = true
        ),
        @CompoundIndex(
                name = "tool_reviews_created_idx",
                def = "{'toolId':1,'createdAt':-1}"
        )
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolReview {

    @Id
    private String id;

    private String toolId;

    @Indexed
    private String userId;
    private String name;

    private int rating;
    private String comment;

    private Instant createdAt;
}
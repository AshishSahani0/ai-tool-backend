package com.example.backend.tool.subcategory.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "subcategories")
@CompoundIndex(name = "category_slug_idx",
        def = "{'categoryId':1,'slug':1}", unique = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubCategory {

    @Id
    private String id;

    private String categoryId;

    private String name;
    private String slug;

    private int order;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
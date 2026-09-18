package com.example.backend.tool.subcategory.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Document(collection = "subcategories")
@CompoundIndexes({
        @CompoundIndex(name = "category_slug_idx", def = "{'categoryId':1,'slug':1}", unique = true),
        @CompoundIndex(name = "active_order_idx", def = "{'active':1,'order':1}"),
        @CompoundIndex(name = "category_active_order_idx", def = "{'categoryId':1,'active':1,'order':1}")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubCategory {

    @Id
    private String id;

    @Indexed
    private String categoryId;

    private String name;
    private String slug;

    @Indexed
    private int order;

    @Indexed
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
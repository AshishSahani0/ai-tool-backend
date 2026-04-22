package com.example.backend.tool.core.model;

import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.enums.PricingType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;




@Document(collection = "tools")
@CompoundIndexes({

        @CompoundIndex(name = "approved_active_idx",
                def = "{'approvalStatus':1,'active':1}"),

        @CompoundIndex(name = "category_approved_active_idx",
                def = "{'categoryId':1,'approvalStatus':1,'active':1}"),

        @CompoundIndex(name = "sub_approved_active_idx",
                def = "{'subCategoryId':1,'approvalStatus':1,'active':1}"),

        @CompoundIndex(name = "views_idx", def = "{'views':-1}"),

        @CompoundIndex(name = "rating_idx", def = "{'rating':-1}"),

        @CompoundIndex(name = "popularity_idx", def = "{'popularityScore':-1}"),


})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {

    @Id
    private String id;


    private String name;

    @Indexed(unique = true)
    private String slug;

    private String website;
    private String shortDescription;
    private String longDescription;
    private String differentiation;

    @Indexed
    private boolean featured;


    private String logoKey;


    @Indexed
    private String categoryId;

    @Indexed
    private String subCategoryId;

    private List<String> hashtags;

    /* PRICING */
    private PricingType pricingType;
    private String pricingDetails;

    /* ADVANCED CONTENT */
    private List<String> pros;
    private List<String> cons;
    private List<String> useCases;
    private List<String> uniqueFeatures;

    /* RATING SYSTEM */
    private double rating;
    private long ratingSum;
    private int reviewsCount;
    private int views;
    private double popularityScore;

    /* STATUS FLAGS */
    private boolean verified;

    @Indexed
    private boolean active;

    @Indexed
    private ApprovalStatus approvalStatus;



    /* SUBMITTER */
    private String submittedByUserId;
    private String submittedByName;
    private String submittedByEmail;

    /* MODERATION */
    private String rejectionReason;
    private Instant approvedAt;
    private Instant rejectedAt;

    /* TIMESTAMPS */
    private Instant createdAt;
    private Instant updatedAt;
}
package com.example.backend.tool.core.repository;

import com.example.backend.tool.category.dto.SubCategoryToolCount;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.enums.PricingType;
import com.example.backend.tool.core.model.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ToolRepository
        extends MongoRepository<Tool, String>, ToolRepositoryCustom {

    @Aggregation(pipeline = {
            "{ $match: { approvalStatus: ?0, active: true } }",
            "{ $group: { _id: '$subCategoryId', count: { $sum: 1 } } }"
    })
    List<SubCategoryToolCount> countToolsBySubCategory(
            ApprovalStatus status
    );

    Page<ToolCardProjection> findByApprovalStatusAndActiveTrue(
            ApprovalStatus status,
            Pageable pageable
    );







    Page<Tool> findBySubmittedByUserIdOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );

    Optional<Tool> findByIdAndSubmittedByUserId(
            String id,
            String userId
    );



    Page<Tool> findByApprovalStatus(
            ApprovalStatus status,
            Pageable pageable
    );





    long countBySubCategoryIdAndApprovalStatusAndActiveTrue(
            String subCategoryId,
            ApprovalStatus status
    );



    boolean existsBySlug(String slug);

    Optional<Tool> findBySlugAndApprovalStatusAndActiveTrue(
            String slug,
            ApprovalStatus status
    );

    List<Tool> findBySlugInAndApprovalStatusAndActiveTrue(
            List<String> slugs,
            ApprovalStatus status
    );


}
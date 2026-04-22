package com.example.backend.tool.admin.service;

import com.example.backend.tool.user.dto.ToolCreateRequest;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository repo;

    // =========================
    // ✅ ADMIN CREATE (AUTO APPROVED + VERIFIED)
    // =========================
    public Tool createAdminTool(ToolCreateRequest req) {
        Tool tool = buildBaseTool(req)
                .approvalStatus(ApprovalStatus.APPROVED)
                .verified(true)
                .active(true)
                .approvedAt(Instant.now())
                .build();

        return repo.save(tool);
    }

    // =========================
    // 🌍 PUBLIC CREATE (PENDING REVIEW)
    // =========================
    public Tool createPublicTool(ToolCreateRequest req) {
        Tool tool = buildBaseTool(req)
                .approvalStatus(ApprovalStatus.PENDING)
                .verified(false)
                .active(false)
                .build();

        return repo.save(tool);
    }

    // =========================
    // 🔧 SHARED BUILDER
    // =========================
    private Tool.ToolBuilder buildBaseTool(ToolCreateRequest req) {
        return Tool.builder()
                /* BASIC */
                .name(req.name())
                .slug(generateUniqueSlug(req.name()))
                .website(req.website())
                .shortDescription(req.shortDescription())
                .longDescription(req.longDescription())
                .differentiation(req.differentiation())

                /* MEDIA */
                .logoKey(req.logoKey())

                /* CATEGORY */
                .categoryId(req.categoryId())
                .subCategoryId(req.subCategoryId())
                .hashtags(req.hashtags())

                /* PRICING */
                .pricingType(req.pricingType())
                .pricingDetails(req.pricingDetails())

                /* ADVANCED CONTENT */
                .pros(req.pros())
                .cons(req.cons())
                .useCases(req.useCases())
                .uniqueFeatures(req.uniqueFeatures())

                /* DEFAULT STATS */
                .rating(0.0)
                .reviewsCount(0)
                .views(0)

                /* MODERATION */
                .rejectionReason(null)

                /* TIMESTAMPS */
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    // =========================
    // 🔥 SLUG GENERATOR (UNIQUE)
    // =========================
    private String generateUniqueSlug(String name) {

        String baseSlug = name
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        String slug = baseSlug;
        int counter = 1;

        while (repo.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}
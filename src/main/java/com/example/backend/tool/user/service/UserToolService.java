package com.example.backend.tool.user.service;

import com.example.backend.tool.dto.ToolResponse;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.tool.user.dto.ToolCreateRequest;
import com.example.backend.tool.user.dto.UserToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserToolService {

    private final ToolRepository repo;

    /* =================================
       SUBMIT TOOL
       ================================= */
    public UserToolResponse submitTool(
            ToolCreateRequest req,
            String firebaseUid
    ) {

        Tool tool = Tool.builder()
                .name(req.name())
                .slug(generateUniqueSlug(req.name()))
                .website(req.website())
                .shortDescription(req.shortDescription())
                .longDescription(req.longDescription())
                .differentiation(req.differentiation())
                .logoKey(req.logoKey())
                .categoryId(req.categoryId())
                .subCategoryId(req.subCategoryId())
                .hashtags(req.hashtags())
                .pricingType(req.pricingType())
                .pricingDetails(req.pricingDetails())
                .pros(req.pros())
                .cons(req.cons())
                .useCases(req.useCases())
                .uniqueFeatures(req.uniqueFeatures())
                .submittedByUserId(firebaseUid)
                .rating(0.0)
                .ratingSum(0)
                .reviewsCount(0)
                .views(0)
                .popularityScore(0)
                .approvalStatus(ApprovalStatus.PENDING)
                .verified(false)
                .active(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Tool saved = repo.save(tool);
        return mapToDashboardResponse(saved);
    }

    /* =================================
       GET MY TOOLS (Dashboard)
       ================================= */
    public Page<UserToolResponse> getMyTools(
            String firebaseUid,
            Pageable pageable
    ) {
        return repo.findBySubmittedByUserIdOrderByCreatedAtDesc(
                        firebaseUid,
                        pageable
                )
                .map(this::mapToDashboardResponse);
    }

    /* =================================
       GET MY TOOL BY ID (FULL DETAIL)
       ================================= */
    public ToolResponse getMyToolById(
            String toolId,
            String firebaseUid
    ) {
        Tool tool = repo.findByIdAndSubmittedByUserId(
                        toolId,
                        firebaseUid
                )
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        return mapToFullResponse(tool);
    }

    /* =================================
       UPDATE TOOL
       ================================= */
    public UserToolResponse updateMyTool(
            String toolId,
            ToolCreateRequest req,
            String firebaseUid
    ) {

        Tool tool = repo.findByIdAndSubmittedByUserId(
                        toolId,
                        firebaseUid
                )
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        if (tool.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Approved tools cannot be edited"
            );
        }

        tool.setName(req.name());
        tool.setWebsite(req.website());
        tool.setShortDescription(req.shortDescription());
        tool.setLongDescription(req.longDescription());
        tool.setDifferentiation(req.differentiation());
        tool.setCategoryId(req.categoryId());
        tool.setSubCategoryId(req.subCategoryId());
        tool.setHashtags(req.hashtags());
        tool.setPricingType(req.pricingType());
        tool.setPricingDetails(req.pricingDetails());
        tool.setLogoKey(req.logoKey());
        tool.setPros(req.pros());
        tool.setCons(req.cons());
        tool.setUseCases(req.useCases());
        tool.setUniqueFeatures(req.uniqueFeatures());

        tool.setApprovalStatus(ApprovalStatus.PENDING);
        tool.setActive(false);
        tool.setRejectionReason(null);
        tool.setUpdatedAt(Instant.now());

        Tool updated = repo.save(tool);
        return mapToDashboardResponse(updated);
    }

    /* =================================
       MAPPERS
       ================================= */

    private UserToolResponse mapToDashboardResponse(Tool tool) {
        return new UserToolResponse(
                tool.getId(),
                tool.getName(),
                tool.getSlug(),
                tool.getShortDescription(),
                tool.getLogoKey(),
                tool.getPricingType(),
                tool.getApprovalStatus(),
                tool.isActive(),
                tool.getRejectionReason(),
                tool.getCreatedAt()
        );
    }

    private ToolResponse mapToFullResponse(Tool tool) {
        return new ToolResponse(
                tool.getId(),
                tool.getSlug(),
                tool.getName(),
                tool.getWebsite(),
                tool.getShortDescription(),
                tool.getLongDescription(),
                tool.getDifferentiation(),
                tool.getLogoKey(),
                tool.getHashtags(),
                tool.getPricingType(),
                tool.getPricingDetails(),
                tool.getPros(),
                tool.getCons(),
                tool.getUseCases(),
                tool.getUniqueFeatures(),
                tool.getRating(),
                tool.getReviewsCount(),
                tool.getViews(),
                tool.isVerified()
        );
    }

    /* =================================
       SLUG GENERATION
       ================================= */
    private String generateUniqueSlug(String name) {

        String baseSlug = slugify(name);
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            try {
                if (!repo.existsBySlug(slug)) {
                    return slug;
                }
                slug = baseSlug + "-" + counter++;
            } catch (DuplicateKeyException e) {
                slug = baseSlug + "-" + counter++;
            }
        }
    }

    private String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
package com.example.backend.tool.user.service;

import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.tool.category.model.Category;
import com.example.backend.tool.category.repository.CategoryRepository;
import com.example.backend.tool.dto.ToolResponse;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.tool.subcategory.repository.SubCategoryRepository;
import com.example.backend.tool.user.dto.ToolCreateRequest;
import com.example.backend.tool.user.dto.UserToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserToolService {

    private final ToolRepository repo;
    private final CategoryRepository categoryRepo;
    private final SubCategoryRepository subCategoryRepo;

    /* =================================
       SUBMIT TOOL
       ================================= */
    public UserToolResponse submitTool(
            ToolCreateRequest req,
            String firebaseUid
    ) {
        validateCategoryAndSubCategory(req.categoryId(), req.subCategoryId());

        Tool tool = Tool.builder()
                .name(req.name().trim())
                .slug(generateUniqueSlug(req.name().trim()))
                .website(req.website().trim())
                .shortDescription(req.shortDescription().trim())
                .longDescription(req.longDescription() != null ? req.longDescription().trim() : null)
                .differentiation(req.differentiation() != null ? req.differentiation().trim() : null)
                .logoKey(req.logoKey())
                .categoryId(req.categoryId())
                .subCategoryId(req.subCategoryId())
                .hashtags(cleanList(req.hashtags()))
                .pricingType(req.pricingType())
                .pricingDetails(req.pricingDetails() != null ? req.pricingDetails().trim() : null)
                .pros(cleanList(req.pros()))
                .cons(cleanList(req.cons()))
                .useCases(cleanList(req.useCases()))
                .uniqueFeatures(cleanList(req.uniqueFeatures()))
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
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + toolId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + toolId));

        if (tool.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new ForbiddenException("Approved tools cannot be edited");
        }

        validateCategoryAndSubCategory(req.categoryId(), req.subCategoryId());

        tool.setName(req.name().trim());
        tool.setWebsite(req.website().trim());
        tool.setShortDescription(req.shortDescription().trim());
        tool.setLongDescription(req.longDescription() != null ? req.longDescription().trim() : null);
        tool.setDifferentiation(req.differentiation() != null ? req.differentiation().trim() : null);
        tool.setCategoryId(req.categoryId());
        tool.setSubCategoryId(req.subCategoryId());
        tool.setHashtags(cleanList(req.hashtags()));
        tool.setPricingType(req.pricingType());
        tool.setPricingDetails(req.pricingDetails() != null ? req.pricingDetails().trim() : null);
        tool.setLogoKey(req.logoKey());
        tool.setPros(cleanList(req.pros()));
        tool.setCons(cleanList(req.cons()));
        tool.setUseCases(cleanList(req.useCases()));
        tool.setUniqueFeatures(cleanList(req.uniqueFeatures()));

        tool.setApprovalStatus(ApprovalStatus.PENDING);
        tool.setActive(false);
        tool.setRejectionReason(null);
        tool.setUpdatedAt(Instant.now());

        Tool updated = repo.save(tool);
        return mapToDashboardResponse(updated);
    }

    private void validateCategoryAndSubCategory(String categoryId, String subCategoryId) {
        categoryRepo.findById(categoryId)
                .filter(Category::isActive)
                .orElseThrow(() -> new BadRequestException("Selected category does not exist or is inactive"));

        subCategoryRepo.findById(subCategoryId)
                .filter(s -> s.isActive() && categoryId.equals(s.getCategoryId()))
                .orElseThrow(() -> new BadRequestException("Selected subcategory does not exist or does not belong to the chosen category"));
    }

    private List<String> cleanList(List<String> list) {
        if (list == null) return List.of();
        return list.stream()
                .filter(s -> s != null && !s.trim().isBlank())
                .map(String::trim)
                .toList();
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
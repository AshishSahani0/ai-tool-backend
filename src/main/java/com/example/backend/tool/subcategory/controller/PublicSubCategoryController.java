package com.example.backend.tool.subcategory.controller;

import com.example.backend.tool.core.repository.ToolRepositoryCustom;
import com.example.backend.tool.core.repository.ToolCardProjection;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.subcategory.dto.SubCategoryResponse;
import com.example.backend.tool.subcategory.model.SubCategory;
import com.example.backend.tool.subcategory.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/subcategories")
@RequiredArgsConstructor
public class PublicSubCategoryController {

    private final ToolRepositoryCustom toolRepository;
    private final SubCategoryRepository subCategoryRepository;

    @GetMapping("/{id}/tools")
    public Page<ToolCardProjection> toolsBySubCategory(
            @PathVariable String id,
            @RequestParam(required = false) String pricingType,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "views") String sortBy
    ) {

        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return toolRepository.findPublicTools(
                id,
                pricingType,
                verified,
                ApprovalStatus.APPROVED,
                pageable
        );
    }

    @GetMapping("/{id}")
    public SubCategoryResponse getSubCategory(@PathVariable String id) {

        return subCategoryRepository.findById(id)
                .filter(SubCategory::isActive)
                .map(sub -> new SubCategoryResponse(
                        sub.getId(),
                        sub.getName(),
                        sub.getSlug(),
                        sub.getOrder()
                ))
                .orElseThrow(() -> new RuntimeException("SubCategory not found"));
    }
}
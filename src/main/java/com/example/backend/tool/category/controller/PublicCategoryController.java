package com.example.backend.tool.category.controller;

import com.example.backend.tool.category.dto.CategoryResponse;
import com.example.backend.tool.dto.SubCategoryWithCount;
import com.example.backend.tool.category.service.CategoryService;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.subcategory.repository.SubCategoryRepository;
import com.example.backend.tool.core.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;
    private final SubCategoryRepository subCategoryRepo;
    private final ToolRepository toolRepo;

    @GetMapping
    public List<CategoryResponse> categories() {
        return categoryService.all();
    }

    @GetMapping("/{categoryId}/subcategories")
    public List<SubCategoryWithCount> subCategories(
            @PathVariable String categoryId
    ) {
        return subCategoryRepo
                .findByCategoryIdAndActiveTrueOrderByOrderAsc(categoryId)
                .stream()
                .map(sub -> new SubCategoryWithCount(
                        sub.getId(),
                        sub.getName(),
                        toolRepo.countBySubCategoryIdAndApprovalStatusAndActiveTrue(
                                sub.getId(),
                                ApprovalStatus.APPROVED
                        )
                ))
                .toList();
    }
}
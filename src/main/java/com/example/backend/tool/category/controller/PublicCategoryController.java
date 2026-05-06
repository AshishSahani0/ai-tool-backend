package com.example.backend.tool.category.controller;

import com.example.backend.tool.category.dto.CategoryResponse;
import com.example.backend.tool.category.dto.CategoryWithSubsResponse;
import com.example.backend.tool.category.dto.SubCategoryToolCount;
import com.example.backend.tool.dto.SubCategoryWithCount;
import com.example.backend.tool.category.service.CategoryService;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.subcategory.repository.SubCategoryRepository;
import com.example.backend.tool.core.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/full")
    public List<CategoryWithSubsResponse> fullCategories() {

        Map<String, Long> toolCounts =
                toolRepo.countToolsBySubCategory(ApprovalStatus.APPROVED)
                        .stream()
                        .collect(Collectors.toMap(
                                SubCategoryToolCount::get_id,
                                SubCategoryToolCount::getCount
                        ));

        return categoryService.all().stream()
                .map(category -> {

                    List<SubCategoryWithCount> subs =
                            subCategoryRepo
                                    .findByCategoryIdAndActiveTrueOrderByOrderAsc(category.id())
                                    .stream()
                                    .map(sub -> new SubCategoryWithCount(
                                            sub.getId(),
                                            sub.getName(),
                                            toolCounts.getOrDefault(sub.getId(), 0L)
                                    ))
                                    .toList();

                    return new CategoryWithSubsResponse(
                            category.id(),
                            category.name(),
                            category.imageKey(),
                            subs
                    );
                })
                .toList();
    }
}
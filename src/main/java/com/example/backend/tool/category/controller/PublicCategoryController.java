package com.example.backend.tool.category.controller;

import com.example.backend.tool.category.dto.CategoryResponse;
import com.example.backend.tool.category.dto.CategoryWithSubsResponse;
import com.example.backend.tool.category.dto.SubCategoryToolCount;
import com.example.backend.tool.dto.SubCategoryWithCount;
import com.example.backend.tool.category.service.CategoryService;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.subcategory.model.SubCategory;
import com.example.backend.tool.subcategory.repository.SubCategoryRepository;
import com.example.backend.tool.core.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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
    public ResponseEntity<List<CategoryResponse>> categories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(categoryService.all());
    }

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubCategoryWithCount>> subCategories(
            @PathVariable String categoryId
    ) {
        List<SubCategory> subs = subCategoryRepo
                .findByCategoryIdAndActiveTrueOrderByOrderAsc(categoryId);

        if (subs.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Map<String, Long> toolCounts =
                toolRepo.countToolsBySubCategory(ApprovalStatus.APPROVED)
                        .stream()
                        .filter(c -> c != null && c.get_id() != null && !c.get_id().isBlank())
                        .collect(Collectors.toMap(
                                SubCategoryToolCount::get_id,
                                SubCategoryToolCount::getCount,
                                (existing, replacement) -> existing
                        ));

        List<SubCategoryWithCount> result = subs.stream()
                .map(sub -> new SubCategoryWithCount(
                        sub.getId(),
                        sub.getName(),
                        toolCounts.getOrDefault(sub.getId(), 0L)
                ))
                .toList();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(result);
    }

    @GetMapping("/full")
    public ResponseEntity<List<CategoryWithSubsResponse>> fullCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic().staleWhileRevalidate(Duration.ofMinutes(10)))
                .body(categoryService.fullCategories());
    }
}
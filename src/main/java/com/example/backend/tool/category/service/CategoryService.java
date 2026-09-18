package com.example.backend.tool.category.service;

import com.example.backend.tool.category.dto.CategoryResponse;
import com.example.backend.tool.dto.CategoryRequest;
import com.example.backend.tool.category.model.Category;
import com.example.backend.tool.category.repository.CategoryRepository;

import com.example.backend.tool.category.dto.CategoryWithSubsResponse;
import com.example.backend.tool.category.dto.SubCategoryToolCount;
import com.example.backend.tool.dto.SubCategoryWithCount;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.subcategory.model.SubCategory;
import com.example.backend.tool.subcategory.repository.SubCategoryRepository;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.media.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repo;
    private final SubCategoryRepository subCategoryRepo;
    private final ToolRepository toolRepo;
    private final R2UploadService r2;


    /* =========================
       CREATE CATEGORY
       ========================= */
    @CacheEvict(value = {"categories_all", "categories_full"}, allEntries = true)
    public CategoryResponse create(CategoryRequest req) {

        Category category = Category.builder()
                .name(req.name())
                .slug(generateUniqueSlug(req.name()))
                .imageKey(req.imageKey())
                .order(req.order())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Category saved = repo.save(category);
        return map(saved);
    }

    /* =========================
       GET ALL ACTIVE
       ========================= */
    @Cacheable(value = "categories_all")
    public List<CategoryResponse> all() {
        return repo.findByActiveTrueOrderByOrderAsc()
                .stream()
                .map(this::map)
                .toList();
    }

    /* =========================
       GET ALL WITH SUBCATEGORIES (CACHED)
       ========================= */
    @Cacheable(value = "categories_full")
    public List<CategoryWithSubsResponse> fullCategories() {

        Map<String, Long> toolCounts =
                toolRepo.countToolsBySubCategory(ApprovalStatus.APPROVED)
                        .stream()
                        .filter(c -> c != null && c.get_id() != null && !c.get_id().isBlank())
                        .collect(Collectors.toMap(
                                SubCategoryToolCount::get_id,
                                SubCategoryToolCount::getCount,
                                (existing, replacement) -> existing
                        ));

        List<SubCategory> allActiveSubs = subCategoryRepo.findByActiveTrueOrderByOrderAsc();
        Map<String, List<SubCategory>> subsByCategory = allActiveSubs.stream()
                .filter(sub -> sub.getCategoryId() != null)
                .collect(Collectors.groupingBy(SubCategory::getCategoryId));

        return repo.findByActiveTrueOrderByOrderAsc().stream()
                .map(category -> {
                    List<SubCategory> categorySubs = subsByCategory.getOrDefault(category.getId(), List.of());
                    List<SubCategoryWithCount> subs = categorySubs.stream()
                            .map(sub -> new SubCategoryWithCount(
                                    sub.getId(),
                                    sub.getName(),
                                    toolCounts.getOrDefault(sub.getId(), 0L)
                            ))
                            .toList();

                    return new CategoryWithSubsResponse(
                            category.getId(),
                            category.getName(),
                            category.getImageKey(),
                            subs
                    );
                })
                .toList();
    }

    /* =========================
       UPDATE IMAGE
       ========================= */
    @CacheEvict(value = {"categories_all", "categories_full"}, allEntries = true)
    public CategoryResponse updateImage(String categoryId, MultipartFile file) {

        Category category = repo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Upload new image
        String newKey = r2.uploadCategoryImage(file);

        // Delete old only if exists
        if (category.getImageKey() != null) {
            r2.delete(category.getImageKey());
        }

        category.setImageKey(newKey);
        category.setUpdatedAt(Instant.now());

        Category updated = repo.save(category);
        return map(updated);
    }

    /* =========================
       SLUG SAFETY
       ========================= */
    private String generateUniqueSlug(String name) {

        String base = slugify(name);
        String slug = base;
        int counter = 1;

        while (true) {
            try {
                if (!repo.existsBySlug(slug)) {
                    return slug;
                }
                slug = base + "-" + counter++;
            } catch (DuplicateKeyException e) {
                slug = base + "-" + counter++;
            }
        }
    }

    private String slugify(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\w\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }

    /* =========================
       MAPPER
       ========================= */
    private CategoryResponse map(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getImageKey(),
                c.getOrder()
        );
    }
}
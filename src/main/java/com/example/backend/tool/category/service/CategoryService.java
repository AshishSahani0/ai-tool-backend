package com.example.backend.tool.category.service;

import com.example.backend.tool.category.dto.CategoryResponse;
import com.example.backend.tool.dto.CategoryRequest;
import com.example.backend.tool.category.model.Category;
import com.example.backend.tool.category.repository.CategoryRepository;

import com.example.backend.media.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repo;
    private final R2UploadService r2;


    /* =========================
       CREATE CATEGORY
       ========================= */
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
    public List<CategoryResponse> all() {
        return repo.findByActiveTrueOrderByOrderAsc()
                .stream()
                .map(this::map)
                .toList();
    }

    /* =========================
       UPDATE IMAGE
       ========================= */
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
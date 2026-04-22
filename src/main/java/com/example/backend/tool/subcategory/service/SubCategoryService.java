package com.example.backend.tool.subcategory.service;

import com.example.backend.tool.subcategory.dto.SubCategoryRequest;
import com.example.backend.tool.subcategory.dto.SubCategoryResponse;
import com.example.backend.tool.subcategory.model.SubCategory;
import com.example.backend.tool.subcategory.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SubCategoryService {

    private final SubCategoryRepository repo;

    public SubCategoryResponse create(SubCategoryRequest req) {

        SubCategory sub = SubCategory.builder()
                .categoryId(req.categoryId())
                .name(req.name())
                .slug(generateUniqueSlug(req.categoryId(), req.name()))
                .order(req.order())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        SubCategory saved = repo.save(sub);
        return map(saved);
    }

    private String generateUniqueSlug(String categoryId, String name) {

        String base = slugify(name);
        String slug = base;
        int counter = 1;

        while (true) {
            try {
                if (!repo.existsByCategoryIdAndSlug(categoryId, slug)) {
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

    private SubCategoryResponse map(SubCategory sub) {
        return new SubCategoryResponse(
                sub.getId(),
                sub.getName(),
                sub.getSlug(),
                sub.getOrder()
        );
    }
}
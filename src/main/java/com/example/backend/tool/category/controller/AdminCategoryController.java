package com.example.backend.tool.category.controller;

import com.example.backend.tool.dto.CategoryRequest;
import com.example.backend.tool.category.dto.CategoryResponse;
import com.example.backend.tool.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService service;

    @PostMapping
    public CategoryResponse create(@RequestBody CategoryRequest req) {
        return service.create(req);
    }

    @GetMapping
    public List<CategoryResponse> all() {
        return service.all();
    }

    @PatchMapping("/{id}/image")
    public CategoryResponse updateCategoryImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file
    ) {
        return service.updateImage(id, file);
    }
}
package com.example.backend.tool.subcategory.controller;

import com.example.backend.tool.subcategory.dto.SubCategoryRequest;
import com.example.backend.tool.subcategory.dto.SubCategoryResponse;
import com.example.backend.tool.subcategory.service.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subcategories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubCategoryController {

    private final SubCategoryService service;

    @PostMapping
    public SubCategoryResponse create(@RequestBody SubCategoryRequest req) {
        return service.create(req);
    }
}
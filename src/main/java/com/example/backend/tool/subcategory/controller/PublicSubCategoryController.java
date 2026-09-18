package com.example.backend.tool.subcategory.controller;

import com.example.backend.tool.dto.ToolCardResponse;
import com.example.backend.tool.public_.service.PublicToolService;
import com.example.backend.tool.subcategory.dto.SubCategoryResponse;
import com.example.backend.tool.subcategory.service.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/subcategories")
@RequiredArgsConstructor
public class PublicSubCategoryController {

    private final PublicToolService publicToolService;
    private final SubCategoryService subCategoryService;

    @GetMapping("/{id}/tools")
    public Page<ToolCardResponse> toolsBySubCategory(
            @PathVariable String id,
            @RequestParam(required = false) String pricingType,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "popularityScore") String sortBy
    ) {
        return publicToolService.filterTools(
                id,
                pricingType,
                verified,
                sortBy,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public SubCategoryResponse getSubCategory(@PathVariable String id) {
        return subCategoryService.getById(id);
    }
}
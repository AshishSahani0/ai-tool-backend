package com.example.backend.tool.public_.controller;

import com.example.backend.tool.dto.ToolCardResponse;
import com.example.backend.tool.dto.ToolResponse;
import com.example.backend.tool.public_.service.PublicToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/tools")
@RequiredArgsConstructor
public class PublicToolController {

    private final PublicToolService service;

    @GetMapping
    public Page<ToolCardResponse> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return service.listApprovedTools(page, size);
    }

    @GetMapping("/{slug}")
    public ToolResponse bySlug(@PathVariable String slug) {
        return service.getBySlug(slug);
    }

    @GetMapping("/filter")
    public Page<ToolCardResponse> filterTools(
            @RequestParam(required = false) String subCategoryId,
            @RequestParam(required = false) String pricingType,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "popularityScore") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        return service.filterTools(
                subCategoryId,
                pricingType,
                verified,
                sortBy,
                page,
                size
        );
    }
}
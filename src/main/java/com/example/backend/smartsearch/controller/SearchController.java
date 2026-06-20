package com.example.backend.smartsearch.controller;



import com.example.backend.smartsearch.dto.SearchResponse;
import com.example.backend.smartsearch.service.SmartSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/search")
@RequiredArgsConstructor
public class SearchController {

    private final SmartSearchService smartSearchService;

    @GetMapping
    public SearchResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String subCategoryId,
            @RequestParam(required = false) String pricingType,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "popularityScore") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return smartSearchService.search(q, subCategoryId, pricingType, verified, sortBy, page, size);
    }
}
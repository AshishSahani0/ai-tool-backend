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
    public SearchResponse search(@RequestParam String q) {
        return new SearchResponse(
                smartSearchService.search(q)
        );
    }
}
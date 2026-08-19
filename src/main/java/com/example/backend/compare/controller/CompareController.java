package com.example.backend.compare.controller;

import com.example.backend.compare.dto.CompareToolResponse;
import com.example.backend.compare.service.CompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public/compare")
@RequiredArgsConstructor
public class CompareController {

    private final CompareService compareService;

    @GetMapping
    public ResponseEntity<List<CompareToolResponse>> getComparisonTools(
            @RequestParam(required = false) List<String> slugs
    ) {
        List<CompareToolResponse> result = compareService.getComparisonTools(slugs);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(result);
    }
}

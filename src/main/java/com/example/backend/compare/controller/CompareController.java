package com.example.backend.compare.controller;

import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.tool.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/compare")
@RequiredArgsConstructor
public class CompareController {

    private final ToolRepository toolRepository;

    @GetMapping
    public List<Tool> getComparisonTools(@RequestParam List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            return List.of();
        }
        // Limit comparison to maximum of 3 tools to preserve layout and backend processing bounds
        List<String> limitedSlugs = slugs.stream()
                .limit(3)
                .toList();

        return toolRepository.findBySlugInAndApprovalStatusAndActiveTrue(
                limitedSlugs,
                ApprovalStatus.APPROVED
        );
    }
}

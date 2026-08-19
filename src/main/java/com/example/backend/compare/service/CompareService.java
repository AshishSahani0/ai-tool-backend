package com.example.backend.compare.service;

import com.example.backend.compare.dto.CompareToolResponse;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import com.example.backend.tool.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompareService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{1,100}$");
    private static final int MAX_COMPARE_TOOLS = 4;

    private final ToolRepository toolRepository;

    public List<CompareToolResponse> getComparisonTools(List<String> rawSlugs) {
        if (rawSlugs == null || rawSlugs.isEmpty()) {
            return List.of();
        }

        // Clean, sanitize, validate against slug regex, deduplicate, and limit to 4
        List<String> validSlugs = rawSlugs.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .filter(s -> SLUG_PATTERN.matcher(s).matches())
                .distinct()
                .limit(MAX_COMPARE_TOOLS)
                .toList();

        if (validSlugs.isEmpty()) {
            return List.of();
        }

        // Query database using compound index { slug: 1, approvalStatus: 1, active: 1 }
        List<Tool> matchedTools = toolRepository.findBySlugInAndApprovalStatusAndActiveTrue(
                validSlugs,
                ApprovalStatus.APPROVED
        );

        if (matchedTools.isEmpty()) {
            return List.of();
        }

        // Lookup map to preserve the exact order requested by the client
        Map<String, Tool> toolBySlug = matchedTools.stream()
                .collect(Collectors.toMap(Tool::getSlug, t -> t, (t1, t2) -> t1));

        return validSlugs.stream()
                .map(toolBySlug::get)
                .filter(Objects::nonNull)
                .map(CompareToolResponse::fromEntity)
                .toList();
    }
}

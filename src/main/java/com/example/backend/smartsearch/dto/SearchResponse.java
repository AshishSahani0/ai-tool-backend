package com.example.backend.smartsearch.dto;

import com.example.backend.tool.core.model.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    private List<Tool> results;
    private int totalPages;
    private long totalElements;
}
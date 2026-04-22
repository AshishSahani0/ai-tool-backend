package com.example.backend.smartsearch.dto;

import com.example.backend.tool.core.model.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private List<Tool> results;
}
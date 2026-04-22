package com.example.backend.tool.admin.controller;

import com.example.backend.tool.user.dto.ToolCreateRequest;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.admin.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tools")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminToolController {

    private final ToolService service;

    @PostMapping
    public Tool create(@RequestBody ToolCreateRequest req) {
        return service.createAdminTool(req);
    }
}
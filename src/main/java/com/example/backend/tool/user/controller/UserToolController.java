package com.example.backend.tool.user.controller;

import com.example.backend.auth.security.AuthPrincipal;
import com.example.backend.common.exception.UnauthorizedException;
import com.example.backend.tool.dto.ToolResponse;
import com.example.backend.tool.user.dto.ToolCreateRequest;
import com.example.backend.tool.user.dto.UserToolResponse;
import com.example.backend.tool.user.service.UserToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/tools")
@RequiredArgsConstructor
public class UserToolController {

    private final UserToolService service;

    @PostMapping
    public UserToolResponse submitTool(
            @Valid @RequestBody ToolCreateRequest req,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to submit tools");
        }

        return service.submitTool(req, principal.getUid());
    }

    @GetMapping
    public Page<UserToolResponse> mySubmittedTools(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to view your tools");
        }

        return service.getMyTools(principal.getUid(), pageable);
    }

    @GetMapping("/{id}")
    public ToolResponse getMyTool(
            @PathVariable String id,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to view tool details");
        }

        return service.getMyToolById(id, principal.getUid());
    }

    @PutMapping("/{id}")
    public UserToolResponse updateMyTool(
            @PathVariable String id,
            @Valid @RequestBody ToolCreateRequest req,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to update tools");
        }

        return service.updateMyTool(id, req, principal.getUid());
    }
}
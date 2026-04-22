package com.example.backend.tool.admin.controller;

import com.example.backend.tool.admin.service.AdminToolApprovalService;
import com.example.backend.tool.dto.RejectToolRequest;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tools")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminToolApprovalController {

    private final AdminToolApprovalService service;
    private final EmailService emailService;

    @GetMapping("/pending")
    public Page<Tool> pending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.pendingTools(page, size);
    }

    @PostMapping("/{id}/approve")
    public Tool approve(@PathVariable String id) {

        Tool saved = service.approve(id);
        emailService.sendToolApprovedEmail(saved);
        return saved;
    }

    @PostMapping("/{id}/reject")
    public Tool reject(
            @PathVariable String id,
            @RequestBody RejectToolRequest req
    ) {
        if (req.reason() == null || req.reason().isBlank()) {
            throw new RuntimeException("Rejection reason is required");
        }

        Tool saved = service.reject(id, req.reason());
        emailService.sendToolRejectedEmail(saved);
        return saved;
    }

    @GetMapping("/approved")
    public Page<Tool> approved(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.findByStatus(ApprovalStatus.APPROVED, page, size);
    }

    @GetMapping("/rejected")
    public Page<Tool> rejected(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.findByStatus(ApprovalStatus.REJECTED, page, size);
    }
}
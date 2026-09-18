package com.example.backend.tool.admin.service;

import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminToolApprovalService {

    private final ToolRepository repo;

    /* =========================
       🔍 FIND BY STATUS (Paginated)
       ========================= */
    public Page<Tool> findByStatus(
            ApprovalStatus status,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);

        return repo.findByApprovalStatus(
                status,
                PageRequest.of(safePage, safeSize)
        );
    }

    /* =========================
       🟡 PENDING TOOLS
       ========================= */
    public Page<Tool> pendingTools(int page, int size) {
        return findByStatus(ApprovalStatus.PENDING, page, size);
    }

    /* =========================
       ✅ APPROVE
       ========================= */
    @CacheEvict(value = {"categories_full", "tool_by_slug", "tools_related"}, allEntries = true)
    public Tool approve(String toolId) {

        Tool tool = repo.findById(toolId)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + toolId));

        tool.setApprovalStatus(ApprovalStatus.APPROVED);
        tool.setActive(true);
        tool.setApprovedAt(Instant.now());
        tool.setRejectedAt(null);
        tool.setRejectionReason(null);
        tool.setUpdatedAt(Instant.now());

        return repo.save(tool);
    }

    /* =========================
       ❌ REJECT
       ========================= */
    @CacheEvict(value = {"categories_full", "tool_by_slug", "tools_related"}, allEntries = true)
    public Tool reject(String toolId, String reason) {

        Tool tool = repo.findById(toolId)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + toolId));

        tool.setApprovalStatus(ApprovalStatus.REJECTED);
        tool.setActive(false);
        tool.setRejectedAt(Instant.now());
        tool.setRejectionReason(reason);
        tool.setUpdatedAt(Instant.now());

        return repo.save(tool);
    }
}
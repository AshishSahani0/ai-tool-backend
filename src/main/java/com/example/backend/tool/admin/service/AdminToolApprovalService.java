package com.example.backend.tool.admin.service;

import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.core.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
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
        return repo.findByApprovalStatus(
                status,
                PageRequest.of(page, size)
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
    public Tool approve(String toolId) {

        Tool tool = repo.findById(toolId)
                .orElseThrow(() -> new RuntimeException("Tool not found"));

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
    public Tool reject(String toolId, String reason) {

        Tool tool = repo.findById(toolId)
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        tool.setApprovalStatus(ApprovalStatus.REJECTED);
        tool.setActive(false);
        tool.setRejectedAt(Instant.now());
        tool.setRejectionReason(reason);
        tool.setUpdatedAt(Instant.now());

        return repo.save(tool);
    }
}
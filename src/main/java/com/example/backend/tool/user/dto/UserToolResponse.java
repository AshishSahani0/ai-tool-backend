package com.example.backend.tool.user.dto;

import com.example.backend.tool.enums.ApprovalStatus;
import com.example.backend.tool.enums.PricingType;

import java.time.Instant;

public record UserToolResponse(
        String id,
        String name,
        String slug,
        String shortDescription,
        String logoKey,
        PricingType pricingType,
        ApprovalStatus approvalStatus,
        boolean active,
        String rejectionReason,
        Instant createdAt
) {}
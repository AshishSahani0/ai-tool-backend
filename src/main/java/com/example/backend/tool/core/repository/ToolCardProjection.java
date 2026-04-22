package com.example.backend.tool.core.repository;

import com.example.backend.tool.enums.PricingType;

import java.util.List;

public interface ToolCardProjection {

    String getSlug();
    String getName();
    String getShortDescription();
    String getLogoKey();
    PricingType getPricingType();

    double getRating();
    int getReviewsCount();
    int getViews();
    boolean isVerified();
    String getWebsite();
    List<String> getHashtags();
}
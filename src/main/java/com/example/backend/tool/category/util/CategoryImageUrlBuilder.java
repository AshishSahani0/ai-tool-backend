package com.example.backend.tool.category.util;

import com.example.backend.tool.category.model.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CategoryImageUrlBuilder {

    @Value("${r2.public-url}")
    private String publicBaseUrl;

    public String build(Category c) {
        if (c.getImageKey() == null) return null;

        long v = c.getUpdatedAt() == null
                ? 0
                : c.getUpdatedAt().toEpochMilli();

        return publicBaseUrl + "/" + c.getImageKey() + "?v=" + v;
    }
}

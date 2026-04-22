package com.example.backend.tool.subcategory.repository;

import com.example.backend.tool.subcategory.model.SubCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends MongoRepository<SubCategory, String> {

    List<SubCategory> findByCategoryIdAndActiveTrueOrderByOrderAsc(String categoryId);

    Optional<SubCategory> findByCategoryIdAndSlugAndActiveTrue(String categoryId, String slug);

    boolean existsByCategoryIdAndSlug(String categoryId, String slug);
}
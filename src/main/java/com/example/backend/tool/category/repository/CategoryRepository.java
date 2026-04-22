package com.example.backend.tool.category.repository;

import com.example.backend.tool.category.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {

    List<Category> findByActiveTrueOrderByOrderAsc();

    boolean existsBySlug(String slug);
}
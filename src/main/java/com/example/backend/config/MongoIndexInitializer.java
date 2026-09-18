package com.example.backend.config;

import com.example.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoIndexInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Starting safe MongoDB index verification and initialization...");
            cleanupDuplicateUsers();
            initializeIndexes();
            log.info("MongoDB index verification and initialization completed successfully.");
        } catch (Exception e) {
            log.warn("Non-fatal issue occurred during MongoDB index initialization: {}", e.getMessage());
        }
    }

    /**
     * Resolves pre-existing duplicate user entries before building unique indexes
     */
    private void cleanupDuplicateUsers() {
        try {
            List<User> allUsers = mongoTemplate.findAll(User.class);
            Map<String, List<User>> usersByUid = new HashMap<>();

            for (User u : allUsers) {
                if (u.getFirebaseUid() != null && !u.getFirebaseUid().isBlank()) {
                    usersByUid.computeIfAbsent(u.getFirebaseUid(), k -> new ArrayList<>()).add(u);
                }
            }

            for (Map.Entry<String, List<User>> entry : usersByUid.entrySet()) {
                List<User> duplicates = entry.getValue();
                if (duplicates.size() > 1) {
                    log.warn("Found {} duplicate records for firebaseUid: {}. Retaining the most complete/recent document and cleaning up remainder.",
                            duplicates.size(), entry.getKey());

                    // Sort: prefer record with non-null id and newest update/creation
                    duplicates.sort((a, b) -> {
                        if (a.getUpdatedAt() != null && b.getUpdatedAt() != null) {
                            return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                        }
                        if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                            return b.getCreatedAt().compareTo(a.getCreatedAt());
                        }
                        return b.getId().compareTo(a.getId());
                    });

                    // Keep the first (best), remove the others
                    for (int i = 1; i < duplicates.size(); i++) {
                        User toRemove = duplicates.get(i);
                        log.info("Removing legacy duplicate user document id: {}", toRemove.getId());
                        mongoTemplate.remove(toRemove);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to complete duplicate user cleanup: {}", e.getMessage());
        }
    }

    private void initializeIndexes() {
        try {
            // 1. users: unique index on firebaseUid
            mongoTemplate.indexOps(User.class).ensureIndex(
                    new Index().on("firebaseUid", Sort.Direction.ASC).unique()
            );

            // 2. users: unique sparse index on email
            mongoTemplate.indexOps(User.class).ensureIndex(
                    new Index().on("email", Sort.Direction.ASC).unique().sparse()
            );

            // 3. tools: compound index on submittedByUserId and approvalStatus
            Document toolCompoundDef = new Document();
            toolCompoundDef.put("submittedByUserId", 1);
            toolCompoundDef.put("approvalStatus", 1);
            mongoTemplate.indexOps("tools").ensureIndex(
                    new CompoundIndexDefinition(toolCompoundDef).named("user_submitted_status_idx")
            );

            // 4. tools: user submissions by date
            Document toolCreatedDef = new Document();
            toolCreatedDef.put("submittedByUserId", 1);
            toolCreatedDef.put("createdAt", -1);
            mongoTemplate.indexOps("tools").ensureIndex(
                    new CompoundIndexDefinition(toolCreatedDef).named("user_submitted_tools_idx")
            );

            // 5. tools: subcategory tools sorted by popularity score (public directory default)
            Document subPopularityDef = new Document();
            subPopularityDef.put("subCategoryId", 1);
            subPopularityDef.put("approvalStatus", 1);
            subPopularityDef.put("active", 1);
            subPopularityDef.put("popularityScore", -1);
            mongoTemplate.indexOps("tools").ensureIndex(
                    new CompoundIndexDefinition(subPopularityDef).named("sub_status_popularity_idx")
            );

            // 6. tools: subcategory aggregation index-covered scan
            Document aggDef = new Document();
            aggDef.put("approvalStatus", 1);
            aggDef.put("active", 1);
            aggDef.put("subCategoryId", 1);
            mongoTemplate.indexOps("tools").ensureIndex(
                    new CompoundIndexDefinition(aggDef).named("approved_active_subcategory_idx")
            );
        } catch (Exception e) {
            log.warn("Error ensuring MongoDB indexes: {}", e.getMessage());
        }
    }
}

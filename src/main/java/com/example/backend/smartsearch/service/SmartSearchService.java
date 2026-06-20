package com.example.backend.smartsearch.service;

import com.example.backend.smartsearch.dto.SearchResponse;
import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SmartSearchService {

    private final MongoTemplate mongoTemplate;

    public SearchResponse search(
            String q,
            String subCategoryId,
            String pricingType,
            Boolean verified,
            String sortBy,
            int page,
            int size
    ) {
        // 1. Build Base Filters
        List<Criteria> filterList = new ArrayList<>();
        filterList.add(Criteria.where("active").is(true));
        filterList.add(Criteria.where("approvalStatus").is(ApprovalStatus.APPROVED));

        if (subCategoryId != null && !subCategoryId.isBlank()) {
            filterList.add(Criteria.where("subCategoryId").is(subCategoryId));
        }
        if (pricingType != null && !pricingType.isBlank()) {
            filterList.add(Criteria.where("pricingType").is(pricingType));
        }
        if (verified != null) {
            filterList.add(Criteria.where("verified").is(verified));
        }

        Criteria filtersCriteria = new Criteria().andOperator(filterList.toArray(new Criteria[0]));

        // Determine sort parameters
        Sort sort;
        if (sortBy == null || sortBy.isBlank() || "popularityScore".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "popularityScore");
        } else {
            sort = Sort.by(Sort.Direction.DESC, sortBy);
        }

        List<Tool> results = new ArrayList<>();
        long totalElements = 0;

        if (q != null && !q.isBlank()) {
            String trimmedQ = q.trim();

            // Run Text Search
            List<Tool> textResults = runTextSearch(trimmedQ, filtersCriteria, sort, page, size);
            results.addAll(textResults);

            // If we don't have enough results to fill the page, fallback to Regex Search
            if (results.size() < size) {
                int remaining = size - results.size();
                List<Tool> regexResults = runRegexSearch(trimmedQ, filtersCriteria, sort, remaining);

                // Merge avoiding duplicates
                Map<String, Tool> merged = new LinkedHashMap<>();
                results.forEach(tool -> merged.put(tool.getId(), tool));
                regexResults.forEach(tool -> merged.putIfAbsent(tool.getId(), tool));
                results = new ArrayList<>(merged.values());
            }

            // Approximate count for paginated search results
            Criteria textSearchCriteria = Criteria.where("$text").is(new Document("$search", trimmedQ));
            Criteria combinedTextCriteria = new Criteria().andOperator(filtersCriteria, textSearchCriteria);
            Query countQuery = new Query(combinedTextCriteria);
            totalElements = mongoTemplate.count(countQuery, Tool.class);

            if (totalElements == 0) {
                // Regex fallback count
                String escapedPattern = Pattern.quote(trimmedQ);
                Pattern pattern = Pattern.compile(escapedPattern, Pattern.CASE_INSENSITIVE);
                Criteria regexCriteria = new Criteria().orOperator(
                        Criteria.where("name").regex(pattern),
                        Criteria.where("shortDescription").regex(pattern),
                        Criteria.where("hashtags").regex(pattern)
                );
                Criteria combinedRegexCriteria = new Criteria().andOperator(filtersCriteria, regexCriteria);
                Query regexCountQuery = new Query(combinedRegexCriteria);
                totalElements = mongoTemplate.count(regexCountQuery, Tool.class);
            }
        } else {
            // No text search term, just standard filter query
            Query query = new Query(filtersCriteria);
            totalElements = mongoTemplate.count(query, Tool.class);

            query.with(PageRequest.of(page, size, sort));
            results = mongoTemplate.find(query, Tool.class);
        }

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new SearchResponse(results, totalPages, totalElements);
    }

    private List<Tool> runTextSearch(String query, Criteria filtersCriteria, Sort sort, int page, int size) {
        List<AggregationOperation> ops = new ArrayList<>();

        // Match stage: text search AND filter criteria
        Criteria textCriteria = new Criteria().andOperator(
                Criteria.where("$text").is(new Document("$search", query)),
                filtersCriteria
        );
        ops.add(Aggregation.match(textCriteria));

        // Add textScore relevance
        ops.add(Aggregation.addFields().addFieldWithValue("textScore", new Document("$meta", "textScore")).build());

        // Sort stage
        ops.add(Aggregation.sort(sort));

        // Pagination stages
        ops.add(Aggregation.skip((long) page * size));
        ops.add(Aggregation.limit(size));

        Aggregation aggregation = Aggregation.newAggregation(ops);

        return mongoTemplate.aggregate(
                aggregation,
                "tools",
                Tool.class
        ).getMappedResults();
    }

    private List<Tool> runRegexSearch(String query, Criteria filtersCriteria, Sort sort, int limit) {
        // Secure quote pattern to prevent Regex Injection and ReDoS
        String escaped = Pattern.quote(query.trim());
        Pattern pattern = Pattern.compile(escaped, Pattern.CASE_INSENSITIVE);

        Criteria regexCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(pattern),
                Criteria.where("shortDescription").regex(pattern),
                Criteria.where("hashtags").regex(pattern)
        );

        Criteria finalCriteria = new Criteria().andOperator(regexCriteria, filtersCriteria);

        Query queryObj = new Query(finalCriteria)
                .with(sort)
                .limit(limit);

        return mongoTemplate.find(queryObj, Tool.class);
    }
}
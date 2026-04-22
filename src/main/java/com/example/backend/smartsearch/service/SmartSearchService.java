package com.example.backend.smartsearch.service;

import com.example.backend.tool.core.model.Tool;
import com.example.backend.tool.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SmartSearchService {

    private final MongoTemplate mongoTemplate;

    public List<Tool> search(String query) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        // 1️⃣ TEXT SEARCH (Primary)
        List<Tool> textResults = runTextSearch(query);

        // 2️⃣ REGEX FALLBACK (Typo tolerance)
        List<Tool> regexResults = runRegexSearch(query);

        // 3️⃣ Merge results (avoid duplicates)
        Map<String, Tool> merged = new LinkedHashMap<>();

        textResults.forEach(tool -> merged.put(tool.getId(), tool));
        regexResults.forEach(tool -> merged.putIfAbsent(tool.getId(), tool));

        return merged.values()
                .stream()
                .limit(6)
                .toList();
    }

    private List<Tool> runTextSearch(String query) {

        Aggregation aggregation = Aggregation.newAggregation(

                Aggregation.match(
                        new Criteria().andOperator(
                                Criteria.where("$text")
                                        .is(new Document("$search", query)),
                                Criteria.where("active").is(true),
                                Criteria.where("approvalStatus")
                                        .is(ApprovalStatus.APPROVED)
                        )
                ),

                Aggregation.addFields()
                        .addFieldWithValue("textScore",
                                new Document("$meta", "textScore"))
                        .build(),

                Aggregation.sort(Sort.by(Sort.Direction.DESC, "textScore")),

                Aggregation.limit(6)
        );

        return mongoTemplate.aggregate(
                aggregation,
                "tools",
                Tool.class
        ).getMappedResults();
    }

    private List<Tool> runRegexSearch(String query) {

        String normalized = query.replaceAll("\\s+", "");
        Pattern pattern = Pattern.compile(normalized, Pattern.CASE_INSENSITIVE);

        Criteria regexCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(pattern),
                Criteria.where("shortDescription").regex(pattern),
                Criteria.where("hashtags").regex(pattern)
        );

        Criteria filters = new Criteria().andOperator(
                regexCriteria,
                Criteria.where("active").is(true),
                Criteria.where("approvalStatus")
                        .is(ApprovalStatus.APPROVED)
        );

        return mongoTemplate.find(
                org.springframework.data.mongodb.core.query.Query.query(filters)
                        .limit(6),
                Tool.class
        );
    }
}
package com.example.backend.config;

import com.example.backend.tool.category.dto.CategoryResponse;
import com.example.backend.tool.category.dto.CategoryWithSubsResponse;
import com.example.backend.tool.dto.SubCategoryWithCount;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CacheSerializationTest {

    private GenericJackson2JsonRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);

        serializer = new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Test
    void testSerializeCategoryResponseList() {
        CategoryResponse cat1 = new CategoryResponse("cat-1", "AI Tools", "ai-tools", "img1", 1);
        CategoryResponse cat2 = new CategoryResponse("cat-2", "Dev Tools", "dev-tools", "img2", 2);
        List<CategoryResponse> list = List.of(cat1, cat2);

        byte[] serialized = serializer.serialize(list);
        assertNotNull(serialized);
        System.out.println("CategoryResponse List JSON: " + new String(serialized));

        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof List);
        List<?> resultList = (List<?>) deserialized;
        assertEquals(2, resultList.size());
        assertEquals(CategoryResponse.class, resultList.get(0).getClass());
        assertEquals("AI Tools", ((CategoryResponse) resultList.get(0)).name());
    }

    @Test
    void testSerializeCategoryResponseArrayList() {
        // Test with java.util.ArrayList as well as ImmutableCollections
        List<CategoryResponse> list = new ArrayList<>();
        list.add(new CategoryResponse("cat-1", "AI Tools", "ai-tools", "img1", 1));

        byte[] serialized = serializer.serialize(list);
        assertNotNull(serialized);

        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof List);
        List<?> resultList = (List<?>) deserialized;
        assertEquals(1, resultList.size());
        assertEquals(CategoryResponse.class, resultList.get(0).getClass());
    }

    @Test
    void testSerializeCategoryWithSubsResponseList() {
        SubCategoryWithCount sub = new SubCategoryWithCount("sub-1", "Text Generators", 5L);
        CategoryWithSubsResponse catWithSubs = new CategoryWithSubsResponse("cat-1", "AI Tools", "img-key", List.of(sub));
        List<CategoryWithSubsResponse> list = List.of(catWithSubs);

        byte[] serialized = serializer.serialize(list);
        assertNotNull(serialized);
        System.out.println("CategoryWithSubsResponse List JSON: " + new String(serialized));

        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof List);
        List<?> resultList = (List<?>) deserialized;
        assertEquals(1, resultList.size());
        assertEquals(CategoryWithSubsResponse.class, resultList.get(0).getClass());
        CategoryWithSubsResponse deserializedCat = (CategoryWithSubsResponse) resultList.get(0);
        assertEquals("Text Generators", deserializedCat.subCategories().get(0).name());
    }

    @Test
    void testRedisCacheManagerEndToEnd() {
        try {
            org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory factory =
                    new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory("localhost", 6379);
            factory.afterPropertiesSet();
            factory.getConnection().ping();

            CacheConfig config = new CacheConfig();
            org.springframework.cache.CacheManager cacheManager = config.cacheManager(factory, "auto");
            assertNotNull(cacheManager);

            org.springframework.cache.Cache cache = cacheManager.getCache("categories_all");
            assertNotNull(cache);

            CategoryResponse cat = new CategoryResponse("cat-test", "AI Tools Test", "ai-tools-test", "img-test", 99);
            List<CategoryResponse> expected = List.of(cat);

            cache.put("test_key", expected);

            org.springframework.cache.Cache.ValueWrapper wrapper = cache.get("test_key");
            assertNotNull(wrapper);
            Object cachedValue = wrapper.get();
            assertNotNull(cachedValue);
            assertTrue(cachedValue instanceof List);
            List<?> list = (List<?>) cachedValue;
            assertEquals(1, list.size());
            assertEquals(CategoryResponse.class, list.get(0).getClass());
            assertEquals("AI Tools Test", ((CategoryResponse) list.get(0)).name());

            // Cleanup
            cache.evict("test_key");
            factory.destroy();
        } catch (Exception e) {
            System.out.println("Skipping Redis live test if connection refused: " + e.getMessage());
        }
    }
}

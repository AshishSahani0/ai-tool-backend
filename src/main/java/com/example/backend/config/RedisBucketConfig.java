package com.example.backend.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "rate.limit.enabled",
        havingValue = "true"
)
public class RedisBucketConfig {

    @org.springframework.beans.factory.annotation.Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @org.springframework.beans.factory.annotation.Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create("redis://" + redisHost + ":" + redisPort);
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient client) {
        return LettuceBasedProxyManager
                .builderFor(client)
                .build();
    }
}
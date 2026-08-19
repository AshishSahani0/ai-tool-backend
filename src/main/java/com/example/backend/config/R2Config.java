package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${r2.access-key:}")
    private String accessKey;

    @Value("${r2.secret-key:}")
    private String secretKey;

    @Value("${r2.endpoint:}")
    private String endpoint;

    @Value("${r2.region:auto}")
    private String region;

    // ONLY CREATE S3 CLIENT
    @Bean
    public S3Client s3Client() {

        var builder = S3Client.builder()
                .region(Region.of(region != null && !region.isBlank() ? region : "auto"));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    )
            );
        }

        return builder.build();
    }
}

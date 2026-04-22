package com.example.backend.auth.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger log =
            LoggerFactory.getLogger(FirebaseConfig.class);

    // 🔥 Read from application.properties / ENV
    @Value("${firebase.config:}")
    private String firebaseConfig;

    @PostConstruct
    public void initFirebase() throws IOException {

        // Prevent double init
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized");
            return;
        }

        InputStream serviceAccount = null;

        
        if (firebaseConfig != null && !firebaseConfig.isBlank()) {
            log.info("Loading Firebase config from ENV");
            serviceAccount = new ByteArrayInputStream(
                    firebaseConfig.getBytes(StandardCharsets.UTF_8)
            );
        }

        if (serviceAccount == null) {
            log.info("Loading Firebase config from file");
            serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase-service.json");
        }

        
        if (serviceAccount == null) {
            throw new RuntimeException("Firebase config not found");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

        log.info("Firebase initialized successfully");
    }
}
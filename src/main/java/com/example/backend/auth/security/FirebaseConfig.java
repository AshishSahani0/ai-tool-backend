package com.example.backend.auth.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log =
            LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initFirebase() throws IOException {

        // 🔥 Prevent double initialization (VERY IMPORTANT)
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized");
            return;
        }

        InputStream serviceAccount =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("firebase-service.json");

        if (serviceAccount == null) {
            throw new RuntimeException(
                    "firebase-service.json not found in src/main/resources"
            );
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

        log.info("Firebase initialized successfully");
    }
}
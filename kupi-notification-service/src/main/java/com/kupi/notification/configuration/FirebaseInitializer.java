package com.kupi.notification.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Firebase configuration
 */
@Configuration
@Slf4j
public class FirebaseInitializer {

    @PostConstruct
    public void initFireBase() throws IOException {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialization was completed {}", FirebaseApp.getInstance());
        } catch (IOException e) {
            log.error("Firebase initialization failed", e);
            // Don't fail startup if Firebase isn't available
        }
    }
}

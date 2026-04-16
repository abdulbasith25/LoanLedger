package com.loanledger.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class NotificationService {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId("loanledger48734")
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("🔥 Firebase Application has been initialized");
            }
        } catch (IOException e) {
            log.error("❌ Firebase initialization failed: {}", e.getMessage());
            log.warn("Make sure GOOGLE_APPLICATION_CREDENTIALS environment variable is set or you are running in a GCP environment.");
        }
    }

    public void sendPushNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            log.warn("Skipping notification: User has no notification token");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("🚀 Successfully sent FCM message: {}", response);
        } catch (Exception e) {
            log.error("❌ Failed to send FCM notification: {}", e.getMessage());
        }
    }
}

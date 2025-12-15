package com.example.geofence_service.service;

import com.example.geofence_service.entity.User;
import com.example.geofence_service.repository.UserRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePushService {

    private final UserRepository userRepository;

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigPath;

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                initialized = true;
            } else {
                initialized = true;
            }
        } catch (IOException e) {
            initialized = false;
        }
    }

    @Async
    public void sendPushNotification(Long userId, String title, String body, String type, Long objectId) {
        if (!initialized) {
            return;
        }

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                log.debug("No FCM token found for user {}", userId);
                return;
            }

            Message message = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", type)
                    .putData("objectId", objectId.toString())
                    .putData("click_action", "/dashboard")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM notification sent to user {}: {}", userId, response);
        } catch (Exception e) {
            log.error("Failed to send FCM notification to user {}", userId, e);
        }
    }
}

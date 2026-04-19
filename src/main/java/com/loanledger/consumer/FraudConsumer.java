package com.loanledger.consumer;

import com.loanledger.events.FraudDetectedEvent;
import com.loanledger.repository.UserRepository;
import com.loanledger.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudConsumer {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @KafkaListener(topics = "fraud-topic", groupId = "fraud-group")
    public void handleFraud(FraudDetectedEvent event) {

        log.warn("🚨 FRAUD EVENT RECEIVED for user {}", event.getUserId());

        // Fetch user to get notification token
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            String title = "Fraud Detection Alert!";
            String body = String.format("Suspicious activity detected: %s. amount associated is %s.",
                    event.getReason(), event.getAmount());

            log.info("🔔 Triggering FCM Notification for User ID: {}", user.getId());
            notificationService.sendPushNotification(user.getNotificationToken(), title, body);
        });
    }
}
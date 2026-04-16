package com.loanledger.consumer;

import com.loanledger.events.LedgerCreatedEvent;
import com.loanledger.repository.UserRepository;
import com.loanledger.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Real-time Kafka Consumer for Ledger Events.
 * This simulates a downstream service (like Audit, Analytics, or Fraud Detection)
 * reacting to new ledger entries immediately as they happen.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerConsumer {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @KafkaListener(topics = "ledger-topic", groupId = "loan-ledger-group")
    public void consumeLedgerEvent(LedgerCreatedEvent event) {
        log.info("📥 [Kafka Consumer] Received Ledger Event: ID={}, User={}, Type={}, Amount={}", 
                event.getLedgerId(), event.getUserId(), event.getType(), event.getAmount());
        
        // Fetch user to get notification token
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            String title = "Transaction Alert: " + event.getType();
            String body = String.format("A new %s of %s has been recorded in your ledger.", 
                    event.getType(), event.getAmount().toString());
            
            log.info("🔔 Triggering FCM Notification for User ID: {}", user.getId());
            notificationService.sendPushNotification(user.getNotificationToken(), title, body);
        });

        if (event.getAmount().doubleValue() > 10000) {
            log.warn("🚨 [HIGH VALUE ALERT] Large transaction detected for User {}: {} {}", 
                    event.getUserId(), event.getAmount(), event.getType());
        }
        
        log.info("✅ Finished processing ledger event for Audit Trail.");
    }
}

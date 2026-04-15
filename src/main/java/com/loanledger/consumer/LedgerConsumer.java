package com.loanledger.consumer;

import com.loanledger.events.LedgerCreatedEvent;
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
public class LedgerConsumer {

    @KafkaListener(topics = "ledger-topic", groupId = "loan-ledger-group")
    public void consumeLedgerEvent(LedgerCreatedEvent event) {
        log.info("📥 [Kafka Consumer] Received Ledger Event: ID={}, User={}, Type={}, Amount={}", 
                event.getLedgerId(), event.getUserId(), event.getType(), event.getAmount());
        
        // Premium Business Logic Simulator:
        // In a real system, you might:
        // 1. Update a real-time analytics dashboard
        // 2. Trigger an alert if amount > 10,000 (Fraud detection)
        // 3. Send a push notification to the user
        
        if (event.getAmount().doubleValue() > 10000) {
            log.warn("🚨 [HIGH VALUE ALERT] Large transaction detected for User {}: {} {}", 
                    event.getUserId(), event.getAmount(), event.getType());
        }
        
        log.info("✅ Finished processing ledger event for Audit Trail.");
    }
}

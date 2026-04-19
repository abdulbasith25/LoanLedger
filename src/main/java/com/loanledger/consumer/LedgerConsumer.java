package com.loanledger.consumer;

import com.loanledger.events.LedgerCreatedEvent;
import com.loanledger.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(topics = "ledger-topic", groupId = "loan-ledger-group")
    public void consumeLedgerEvent(LedgerCreatedEvent event) {
        log.info("📥 [Kafka Consumer] Received Ledger Event: ID={}, User={}, Type={}, Amount={}", 
                event.getLedgerId(), event.getUserId(), event.getType(), event.getAmount());

        
        fraudDetectionService.analyzeTransaction(event);

        log.info("✅ Finished processing ledger event for Audit Trail.");
    }
}

package com.loanledger.service;

import com.loanledger.dto.LedgerEntryDto;
import com.loanledger.entity.LedgerEntry;
import com.loanledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.loanledger.events.LedgerCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final KafkaTemplate<String, Object> kafkaProducer;
    private final UserService userService;

    @Transactional
    public void record(Long userId, LedgerEntry.LedgerType type, BigDecimal amount, String referenceId) {
        String lastHash = ledgerRepository.findFirstByOrderByIdDesc()
                .map(LedgerEntry::getEntryHash)
                .orElse("0");

        LedgerEntry entry = new LedgerEntry();
        entry.setUserId(userId);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setReferenceId(referenceId);
        entry.setPreviousHash(lastHash);
        
        String dataToHash = lastHash + userId + type + amount + referenceId;
        entry.setEntryHash(calculateHash(dataToHash));
        
        // Save first to generate ID
        LedgerEntry savedEntry = ledgerRepository.save(entry);
        
        // Publish event for Kafka downstream processing
        applicationEventPublisher.publishEvent(new LedgerCreatedEvent(
                savedEntry.getId(), 
                userId, 
                amount, 
                type.name(),
                referenceId
        ));
    }


    private String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating ledger hash", e);
        }
    }

    public List<LedgerEntryDto> getLedger(Long userId) {
        return ledgerRepository.findByUserId(userId).stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    private LedgerEntryDto mapToDto(LedgerEntry entry) {
        LedgerEntryDto dto = new LedgerEntryDto();
        dto.setId(entry.getId());
        dto.setUserId(entry.getUserId());
        dto.setType(entry.getType());
        dto.setAmount(entry.getAmount());
        dto.setReferenceId(entry.getReferenceId());
        dto.setEntryHash(entry.getEntryHash());
        dto.setCreatedAt(entry.getCreatedAt());
        return dto;
    }

    @EventListener
    @Async
    public void ledgerEvent(LedgerCreatedEvent event){
        log.info("🚀 Processing Score for Ledger Event: {}", event.getLedgerId());
        userService.incrementScore(event.getUserId(), calculateScore(event));
        kafkaProducer.send("ledger-topic", event);
    }

    /**
     * Core Credit Scoring Logic (The "Elliot" Logic)
     * Rewards punctual repayments and liquidity, while ignoring debt dispersion.
     */
    private int calculateScore(LedgerCreatedEvent event) {
        int score = 0;
        String type = event.getType();
        String ref = event.getReferenceId() != null ? event.getReferenceId() : "";

        // 1. Repayment Reward (The most important factor)
        if ("DEBIT".equals(type) && ref.startsWith("INST-")) {
            score += 20; // High reward for paying back installments
        }

        // 2. Liquidity Reward (Depositing own money into wallet)
        if ("CREDIT".equals(type) && !ref.startsWith("DISB-")) {
            score += 5; 
        }

        // 3. High Volume Transaction Bonus
        if (event.getAmount().doubleValue() > 1000) {
            score += 2;
        }

        // 4. Default baseline for any activity
        score += 1;

        log.info("📊 Calculated Score Increment: {} for User: {}", score, event.getUserId());
        return score;
    }   
}

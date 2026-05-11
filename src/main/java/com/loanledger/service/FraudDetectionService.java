    package com.loanledger.service;

    import com.loanledger.events.FraudDetectedEvent;
    import com.loanledger.events.LedgerCreatedEvent;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import java.util.ArrayDeque;
    import java.util.Deque;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.kafka.core.KafkaTemplate;
    import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class FraudDetectionService {
        @Value("${fraud.max-txn}")
        private int MAX_TXN;
        @Value("${fraud.window-ms}")
        private long WINDOW_MS;

        private final Map<Long, Deque<Long>> userTransactions = new ConcurrentHashMap<>();
        
        private final KafkaTemplate<String, Object> kafkaTemplate;

        public void analyzeTransaction(LedgerCreatedEvent event) {

            long userId = event.getUserId();
            long now = System.currentTimeMillis();

            userTransactions.putIfAbsent(userId, new ArrayDeque<>());
            Deque<Long> timestamps = userTransactions.get(userId);

            synchronized (timestamps) {

                timestamps.addLast(now);

                while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                    timestamps.pollFirst();
                }

                if (timestamps.size() >= MAX_TXN) {
                    kafkaTemplate.send("fraud-topic",
                    new FraudDetectedEvent(userId, event.getLedgerId(),
                    "Too many transactions in short time", event.getAmount().doubleValue()));
                }
            }

            log.info("🛡️ Fraud analysis done for Ledger ID: {}", event.getLedgerId());
        
        }
    }
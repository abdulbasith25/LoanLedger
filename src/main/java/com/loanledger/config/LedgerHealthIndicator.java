package com.loanledger.config;

import com.loanledger.service.LedgerIntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class LedgerHealthIndicator implements HealthIndicator {

    private final LedgerIntegrityService ledgerIntegrityService;

    @Override
    public Health health() {
        boolean isLedgerValid = ledgerIntegrityService.validateChain();
        
        if (isLedgerValid) {
            health =  Health.up()
                    .withDetail("message", "Ledger cryptographic chain is intact")
                    .build();
                    return health;
        } else {
            health =  Health.down()
                    .withDetail("error", "Ledger tampering detected! Cryptographic chain is broken.")
                    .build();
                    return health;
        }
    }
}

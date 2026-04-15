package com.loanledger.service;

import com.loanledger.entity.LedgerEntry;
import com.loanledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerIntegrityService {
    private final LedgerRepository ledgerRepository;

    public boolean validateChain() {
        List<LedgerEntry> entries = ledgerRepository.findAll();
        String expectedPreviousHash = "0";

        for (LedgerEntry entry : entries) {
            // 1. Check if previous hash matches what the current entry claims
            if (!entry.getPreviousHash().equals(expectedPreviousHash)) {
                log.error("❌ Ledger Integrity Violation! Entry {} claims previous hash {} but expected {}", 
                        entry.getId(), entry.getPreviousHash(), expectedPreviousHash);
                return false;
            }

            // 2. Re-calculate the current entry's hash and compare
            String dataToHash = entry.getPreviousHash() + entry.getUserId() + entry.getType() + entry.getAmount() + entry.getReferenceId();
            String recalculatedHash = calculateHash(dataToHash);

            if (!entry.getEntryHash().equals(recalculatedHash)) {
                log.error("❌ Ledger Integrity Violation! Entry {} has been tampered. Stored hash: {}, Recalculated: {}", 
                        entry.getId(), entry.getEntryHash(), recalculatedHash);
                return false;
            }

            expectedPreviousHash = entry.getEntryHash();
        }

        log.info("✅ Ledger Integrity Check Passed. Successfully verified {} entries.", entries.size());
        return true;
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
            return null;
        }
    }
}

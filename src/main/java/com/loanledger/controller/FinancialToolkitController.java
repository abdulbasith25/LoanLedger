package com.loanledger.controller;

import com.loanledger.dto.LoanSimulationResult;
import com.loanledger.service.FinancialToolkitService;
import com.loanledger.service.LedgerService;
import com.loanledger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/utils")
@RequiredArgsConstructor
public class FinancialToolkitController {

    private final FinancialToolkitService toolkitService;
    private final LedgerService ledgerService;
    private final UserService userService;
    private final com.loanledger.service.LedgerIntegrityService integrityService;

    @GetMapping("/simulate")
    public ResponseEntity<LoanSimulationResult> simulateLoan(
            Principal principal,
            @RequestParam BigDecimal amount,
            @RequestParam int tenure) {
        
        Long userId = userService.getUserIdFromUsername(principal.getName());
        return ResponseEntity.ok(toolkitService.simulateLoan(userId, amount, tenure));
    }

    @GetMapping("/export-ledger")
    public ResponseEntity<byte[]> exportLedger(Principal principal) {
        Long userId = userService.getUserIdFromUsername(principal.getName());
        var entries = ledgerService.getLedger(userId);
        String csvData = toolkitService.generateLedgerCsv(entries);
        
        byte[] csvBytes = csvData.getBytes();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ledger_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    @GetMapping("/integrity-check")
    public ResponseEntity<String> checkIntegrity() {
        boolean isValid = integrityService.validateChain();
        if (isValid) {
            return ResponseEntity.ok("✅ Ledger Integrity Verified: All transactions are cryptographically secure.");
        } else {
            return ResponseEntity.status(500).body("❌ Ledger Integrity Violation Detected! Contact administrator.");
        }
    }
}

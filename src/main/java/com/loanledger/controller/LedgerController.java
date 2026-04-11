package com.loanledger.controller;

import com.loanledger.dto.LedgerEntryDto;
import com.loanledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ledger")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LedgerController {
    private final LedgerService ledgerService;

    @GetMapping("/{userId}")
    public List<LedgerEntryDto> getEntries(@PathVariable Long userId) {
        return ledgerService.getLedger(userId);
    }
}

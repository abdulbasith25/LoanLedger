package com.loanledger.service;

import com.loanledger.dto.LedgerEntryDto;
import com.loanledger.entity.LedgerEntry;
import com.loanledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;

    public void record(Long userId, LedgerEntry.LedgerType type, BigDecimal amount, String referenceId) {
        LedgerEntry entry = new LedgerEntry();
        entry.setUserId(userId);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setReferenceId(referenceId);
        ledgerRepository.save(entry);
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
        dto.setCreatedAt(entry.getCreatedAt());
        return dto;
    }
}

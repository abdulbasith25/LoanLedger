package com.loanledger.dto;

import com.loanledger.entity.LedgerEntry.LedgerType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LedgerEntryDto {
    private Long id;
    private Long userId;
    private LedgerType type;
    private BigDecimal amount;
    private String referenceId;
    private String entryHash;
    private LocalDateTime createdAt;
}

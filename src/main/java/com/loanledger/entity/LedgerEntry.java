package com.loanledger.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private LedgerType type;
    private BigDecimal amount;
    private String referenceId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum LedgerType {
        DEBIT, CREDIT
    }
}

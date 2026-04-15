package com.loanledger.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedgerCreatedEvent {
    private Long ledgerId;
    private Long userId;
    private BigDecimal amount;
    private String type;
}
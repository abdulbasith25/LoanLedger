package com.loanledger.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisbursalEvent {
    private Long loanId;
    private Long userId;
    private BigDecimal amount;
    private Integer tenure;
}

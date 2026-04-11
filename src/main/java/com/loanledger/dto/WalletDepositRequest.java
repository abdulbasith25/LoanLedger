package com.loanledger.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletDepositRequest {
    private Long userId;
    private BigDecimal amount;
}

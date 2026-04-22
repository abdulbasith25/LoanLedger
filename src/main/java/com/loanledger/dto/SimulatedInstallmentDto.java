package com.loanledger.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SimulatedInstallmentDto {
    private int installmentNumber;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal principalPortion;
    private BigDecimal interestPortion;
    private BigDecimal remainingBalance;
}

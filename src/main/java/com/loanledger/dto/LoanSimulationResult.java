package com.loanledger.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class LoanSimulationResult {
    private BigDecimal requestedAmount;
    private int tenureMonths;
    private BigDecimal annualInterestRate;
    private BigDecimal monthlyEMI;
    private BigDecimal totalInterestPayable;
    private BigDecimal totalAmountPayable;
    private String recommendation;
    private BigDecimal potentialSavingsNextTier;
    private List<SimulatedInstallmentDto> schedule;
}

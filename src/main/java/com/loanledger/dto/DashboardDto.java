package com.loanledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private BigDecimal totalLiquidity;
    private long activeLoans;
    private String creditHealthStatus;
    private List<LedgerEntryDto> recentTransactions;
}

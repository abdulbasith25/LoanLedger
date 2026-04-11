package com.loanledger.dto;

import com.loanledger.entity.Loan.LoanStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanDto {
    private Long id;
    private Long userId;
    private Long loanProductId;
    private BigDecimal remainingAmount;
    private LoanStatus status;
}

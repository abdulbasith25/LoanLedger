package com.loanledger.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanProductDto {
    private Long id;
    private String title;
    private BigDecimal totalAmount;
    private Double interestRate;
    private Integer tenureMonths;
}

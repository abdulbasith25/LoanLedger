package com.loanledger.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long loanProductId;
    private BigDecimal remainingAmount;
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    public enum LoanStatus {
        PENDING, APPROVED, IN_DISBURSAL, DISBURSED, CLOSED, REJECTED
    }
}

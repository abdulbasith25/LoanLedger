package com.loanledger.dto;

import com.loanledger.entity.Installment.InstallmentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InstallmentDto {
    private Long id;
    private Long loanId;
    private LocalDate dueDate;
    private BigDecimal amount;
    private InstallmentStatus status;
}

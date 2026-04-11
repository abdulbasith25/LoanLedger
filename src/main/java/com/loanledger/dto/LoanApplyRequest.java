package com.loanledger.dto;

import lombok.Data;

@Data
public class LoanApplyRequest {
    private Long userId;
    private Long loanProductId;
}

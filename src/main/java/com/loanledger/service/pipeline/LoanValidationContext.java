package com.loanledger.service.pipeline;

import com.loanledger.entity.LoanProduct;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanValidationContext {
    private final Long userId;
    private final Long loanProductId;
    private LoanProduct loanProduct;

    public LoanValidationContext(Long userId, Long loanProductId) {
        this.userId = userId;
        this.loanProductId = loanProductId;
    }
}

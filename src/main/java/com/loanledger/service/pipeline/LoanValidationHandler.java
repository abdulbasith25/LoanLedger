package com.loanledger.service.pipeline;

public interface LoanValidationHandler {
    void setNext(LoanValidationHandler next);
    void handle(LoanValidationContext context);
}

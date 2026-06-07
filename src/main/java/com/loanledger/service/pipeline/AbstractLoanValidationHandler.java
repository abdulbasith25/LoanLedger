package com.loanledger.service.pipeline;

public abstract class AbstractLoanValidationHandler implements LoanValidationHandler {
    protected LoanValidationHandler next;

    @Override
    public void setNext(LoanValidationHandler next) {
        this.next = next;
    }

    protected void executeNext(LoanValidationContext context) {
        if (next != null) {
            next.handle(context);
        }
    }
}

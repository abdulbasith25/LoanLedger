package com.loanledger.exception;

public class LoanValidationException extends RuntimeException {
    public LoanValidationException(String message) {
        super(message);
    }
}

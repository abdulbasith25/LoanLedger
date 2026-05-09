package com.loanledger.exception;

public class UserAleadyExistException extends RuntimeException {
    public UserAleadyExistException(String message) {
        super(message);
    }
}
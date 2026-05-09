package com.loanledger.exception;

public class UserNotFoundExcpetion extends RuntimeException{
    public UserNotFoundExcpetion(String message){
        super(message);
    }
}
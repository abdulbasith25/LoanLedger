package com.loanledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LoanLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanLedgerApplication.class, args);
    }
}

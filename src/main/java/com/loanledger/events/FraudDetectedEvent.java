package com.loanledger.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudDetectedEvent {
    private long userId;
    private long ledgerId;
    private String reason;
    private double amount;
}

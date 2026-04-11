package com.loanledger.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserDto {
    private Long id;
    private String name;
    private BigDecimal walletBalance;
}

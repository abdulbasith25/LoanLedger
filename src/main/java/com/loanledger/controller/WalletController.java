package com.loanledger.controller;

import com.loanledger.dto.UserCreateRequest;
import com.loanledger.dto.UserDto;
import com.loanledger.dto.WalletDepositRequest;
import com.loanledger.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/deposit")
    public String deposit(@RequestBody WalletDepositRequest request) {
        walletService.deposit(request.getUserId(), request.getAmount(), "WALLET_DEP");
        return "Deposit successful";
    }

    @GetMapping("/{userId}/balance")
    public BigDecimal getBalance(@PathVariable Long userId) {
        return walletService.getBalance(userId);
    }

    @PostMapping("/users")
    public UserDto createUser(@RequestBody UserCreateRequest request) {
        return walletService.createUser(request.getName());
    }
}

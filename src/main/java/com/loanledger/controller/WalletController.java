package com.loanledger.controller;

import com.loanledger.dto.UserCreateRequest;
import com.loanledger.dto.UserDto;
import com.loanledger.dto.WalletDepositRequest;
import com.loanledger.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService; 

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody WalletDepositRequest request) {
        walletService.deposit(request.getUserId(), request.getAmount(), "WALLET_DEP");
        return "Deposit successful";
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return new ResponseEntity<>(balance, HttpStatus.OK);
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
        return walletService.createUser(request.getName());
    }
}

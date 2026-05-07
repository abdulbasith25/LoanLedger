package com.loanledger.service;

import com.loanledger.aspect.Auditing;
import com.loanledger.dto.UserDto;
import com.loanledger.entity.LedgerEntry;
import com.loanledger.entity.User;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserRepository userRepository;
    private final LedgerService ledgerService;

    @Transactional 
    public void deposit(Long userId, BigDecimal amount, String referenceId) {
        User user = userRepository.findByIdWithLock(userId).orElseThrow();
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);
        ledgerService.record(userId, LedgerEntry.LedgerType.CREDIT, amount, referenceId);
    }

    @Transactional
    @Auditing(action = "WALLET_CREDIT")
    public void credit(Long userId, BigDecimal amount, String referenceId) {
        User user = userRepository.findByIdWithLock(userId).orElseThrow();
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);
        ledgerService.record(userId, LedgerEntry.LedgerType.CREDIT, amount, referenceId);
    }

    @Transactional
    @Auditing(action = "WALLET_DEBIT")
    public void debit(Long userId, BigDecimal amount, String referenceId) {
        User user = userRepository.findByIdWithLock(userId).orElseThrow();
        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }
        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        userRepository.save(user);
        ledgerService.record(userId, LedgerEntry.LedgerType.DEBIT, amount, referenceId);
    }

    public BigDecimal getBalance(Long userId) {
        return userRepository.findById(userId).map(User::getWalletBalance).orElse(BigDecimal.ZERO);
    }

    public UserDto createUser(String name) {
        User user = new User();
        user.setName(name);
        User saved = userRepository.save(user);
        return mapToDto(saved);
    }
    
    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setWalletBalance(user.getWalletBalance());
        return dto;
    }
}

package com.loanledger.service;

import com.loanledger.dto.DashboardDto;
import com.loanledger.dto.LedgerEntryDto;
import com.loanledger.entity.LedgerEntry;
import com.loanledger.entity.Loan;
import com.loanledger.repository.LedgerRepository;
import com.loanledger.repository.LoanRepository;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LedgerRepository ledgerRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public DashboardDto getStats() {
        // Sum all wallet balances for liquidity
        BigDecimal totalLiquidity = userRepository.findAll().stream()
                .map(u -> u.getWalletBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count active (disbursed) loans
        long activeLoans = loanRepository.findAll().stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.DISBURSED)
                .count();

        // Get 10 most recent transactions
        List<LedgerEntryDto> recent = ledgerRepository.findAll(PageRequest.of(0, 10, Sort.by("id").descending())).getContent()
                .stream().map(this::mapToDto).collect(Collectors.toList());

        return new DashboardDto(
                totalLiquidity,
                activeLoans,
                activeLoans > 10 ? "At Capacity" : "Optimal",
                recent
        );
    }

    private LedgerEntryDto mapToDto(LedgerEntry entry) {
        LedgerEntryDto dto = new LedgerEntryDto();
        dto.setId(entry.getId());
        dto.setUserId(entry.getUserId());
        dto.setType(entry.getType());
        dto.setAmount(entry.getAmount());
        dto.setCreatedAt(entry.getCreatedAt());
        return dto;
    }
}

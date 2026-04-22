package com.loanledger.service;

import com.loanledger.dto.LoanSimulationResult;
import com.loanledger.dto.SimulatedInstallmentDto;
import com.loanledger.entity.User;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialToolkitService {

    private final UserRepository userRepository;

    public LoanSimulationResult simulateLoan(Long userId, BigDecimal amount, int tenure) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal annualRate = getRateByScore(user.getScore());
        return calculateSimulation(amount, annualRate, tenure);
    }

    public String generateLedgerCsv(List<com.loanledger.dto.LedgerEntryDto> entries) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Date,Type,Amount,Reference,Status\n");
        
        for (var entry : entries) {
            csv.append(entry.getId()).append(",")
               .append(entry.getCreatedAt()).append(",")
               .append(entry.getType()).append(",")
               .append(entry.getAmount()).append(",")
               .append(entry.getReferenceId()).append(",")
               .append("VERIFIED\n");
        }
        return csv.toString();
    }

    private LoanSimulationResult calculateSimulation(BigDecimal principal, BigDecimal annualRate, int tenure) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal emi = calculateEmi(principal, annualRate, tenure);
        
        List<SimulatedInstallmentDto> schedule = new ArrayList<>();
        BigDecimal remainingBalance = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;
        LocalDate baseDate = LocalDate.now();

        for (int i = 1; i <= tenure; i++) {
            BigDecimal interestForMonth = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalForMonth = emi.subtract(interestForMonth);

            if (i == tenure) {
                principalForMonth = remainingBalance;
                emi = principalForMonth.add(interestForMonth);
            }

            totalInterest = totalInterest.add(interestForMonth);
            remainingBalance = remainingBalance.subtract(principalForMonth);

            schedule.add(SimulatedInstallmentDto.builder()
                    .installmentNumber(i)
                    .dueDate(baseDate.plusMonths(i))
                    .totalAmount(emi)
                    .principalPortion(principalForMonth)
                    .interestPortion(interestForMonth)
                    .remainingBalance(remainingBalance.max(BigDecimal.ZERO))
                    .build());
        }

        return LoanSimulationResult.builder()
                .requestedAmount(principal)
                .tenureMonths(tenure)
                .annualInterestRate(annualRate)
                .monthlyEMI(emi)
                .totalInterestPayable(totalInterest)
                .totalAmountPayable(principal.add(totalInterest))
                .schedule(schedule)
                .build();
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenure) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
        }
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRPowerN = (BigDecimal.ONE.add(monthlyRate)).pow(tenure);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRateByScore(int score) {
        if (score >= 900) return new BigDecimal("0.01");
        if (score >= 750) return new BigDecimal("0.02");
        if (score >= 600) return new BigDecimal("0.03");
        return new BigDecimal("0.04");
    }
}

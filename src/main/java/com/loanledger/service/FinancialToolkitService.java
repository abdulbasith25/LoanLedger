package com.loanledger.service;

import com.loanledger.dto.LoanSimulationResult;
import com.loanledger.repository.UserRepository;
import com.loanledger.dto.SimulatedInstallmentDto;
import com.loanledger.entity.User;
import com.loanledger.exception.ResourceNotFoundException;
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
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }
        if (tenure <= 0) {
            throw new IllegalArgumentException("Tenure must be at least 1 month");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        BigDecimal annualRate = getRateByScore(user.getScore());
        LoanSimulationResult result = calculateSimulation(amount, annualRate, tenure);
        result.setRecommendation(getRecommendation(user.getScore()));
        result.setPotentialSavingsNextTier(calculatePotentialSavings(user.getScore(), amount, tenure));
        return result;
    }

    public String generateLedgerCsv(List<com.loanledger.dto.LedgerEntryDto> entries) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Date,Type,Amount,Reference,Status\n");
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var entry : entries) {
            csv.append(entry.getId()).append(",")
               .append(entry.getCreatedAt()).append(",")
               .append(entry.getType()).append(",")
               .append(entry.getAmount()).append(",")
               .append(entry.getReferenceId()).append(",")
               .append("VERIFIED\n");
            
            if (entry.getAmount() != null) {
                totalAmount = totalAmount.add(entry.getAmount());
            }
        }
        
        // Add a summary row
        csv.append("TOTAL,,,,,").append(totalAmount).append("\n");
        
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
        if (score >= 900) return new BigDecimal("0.05"); // 5% APR
        if (score >= 750) return new BigDecimal("0.08"); // 8% APR
        if (score >= 600) return new BigDecimal("0.12"); // 12% APR
        return new BigDecimal("0.18"); // 18% APR
    }

    private String getRecommendation(int score) {
        if (score >= 900) return "Excellent! You qualify for our lowest premium rates.";
        if (score >= 750) return "Great! You have a strong credit profile with competitive rates.";
        if (score >= 600) return "Good. Consider improving your score to unlock sub-10% interest rates.";
        return "Fair. We recommend building your credit score for better future offers.";
    }

    private BigDecimal calculatePotentialSavings(int currentScore, BigDecimal amount, int tenure) {
        int nextTierScore;
        if (currentScore >= 900) return BigDecimal.ZERO;
        else if (currentScore >= 750) nextTierScore = 900;
        else if (currentScore >= 600) nextTierScore = 750;
        else nextTierScore = 600;

        BigDecimal currentInterest = calculateSimulation(amount, getRateByScore(currentScore), tenure).getTotalInterestPayable();
        BigDecimal nextTierInterest = calculateSimulation(amount, getRateByScore(nextTierScore), tenure).getTotalInterestPayable();
        
        return currentInterest.subtract(nextTierInterest).max(BigDecimal.ZERO);
    }
}

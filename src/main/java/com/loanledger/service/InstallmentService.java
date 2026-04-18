package com.loanledger.service;

import com.loanledger.dto.InstallmentDto;
import com.loanledger.entity.Installment;
import com.loanledger.entity.InstallmentStatus;
import com.loanledger.entity.Loan;
import com.loanledger.entity.User;
import com.loanledger.repository.InstallmentRepository;
import com.loanledger.repository.LoanRepository;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstallmentService {
    private final InstallmentRepository installmentRepository;
    private final WalletService walletService;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public void generateInstallments(Loan loan, BigDecimal amount, Integer tenure) {
        User user = userRepository.findById(loan.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal annualRate = getScore(user.getScore());
        BigDecimal installmentAmount = calculateEmi(amount, annualRate, tenure);

        List<Installment> installments = new ArrayList<>();
        LocalDate baseDate = LocalDate.now();

        for (int i = 1; i <= tenure; i++) {
            Installment inst = new Installment();
            inst.setLoanId(loan.getId());
            inst.setDueDate(baseDate.plusMonths(i));
            inst.setAmount(installmentAmount);
            inst.setStatus(InstallmentStatus.PENDING);
            installments.add(inst);
        }
        installmentRepository.saveAll(installments);
        
        // Update loan remaining amount to include the calculated interest
        loan.setRemainingAmount(installmentAmount.multiply(BigDecimal.valueOf(tenure)));
        loanRepository.save(loan);
    }

    private BigDecimal getScore(int score){
        if (score >= 900) 
            return new BigDecimal("0.01");
        else if (score >= 750) 
            return new BigDecimal("0.02");
        else if (score >= 600) 
            return new BigDecimal("0.03");
        else 
            return new BigDecimal("0.04");
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenure) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusRPowerN = (BigDecimal.ONE.add(monthlyRate))
                .pow(tenure);

        BigDecimal numerator = principal
                .multiply(monthlyRate)
                .multiply(onePlusRPowerN);

        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void payInstallment(Long installmentId) {
        Installment installment = installmentRepository.findByIdWithLock(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new RuntimeException("Installment already paid");
        }

        Loan loan = loanRepository.findById(installment.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        User user = userRepository.findById(loan.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        int score = user.getScore();

        BigDecimal finalAmount = installment.getAmount();
        
        long daysOverdue = ChronoUnit.DAYS.between(installment.getDueDate(), LocalDate.now());

       
        if (daysOverdue > 0) {
            BigDecimal penaltyPerDay = getPenaltyRate(user.getScore());
            BigDecimal totalPenalty = penaltyPerDay.multiply(BigDecimal.valueOf(daysOverdue));
            finalAmount = finalAmount.add(totalPenalty);
        }   


        walletService.debit(loan.getUserId(), finalAmount, "INST-" + installmentId);

        installment.setStatus(InstallmentStatus.PAID);
        installmentRepository.save(installment);

        loan.setRemainingAmount(loan.getRemainingAmount().subtract(installment.getAmount()));
        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(Loan.LoanStatus.CLOSED);
        }
        loanRepository.save(loan);
    }

    private BigDecimal getPenaltyRate(int score) {
        if (score >= 900) return new BigDecimal("0.50"); // $0.50 fine per day
        if (score >= 800) return new BigDecimal("1.00"); // $1.00 fine per day
        if (score >= 700) return new BigDecimal("1.50"); // $1.50 fine per day
        return new BigDecimal("2.00");                   // $2.00 fine per day for low scores
    }

    public List<InstallmentDto> getInstallmentsByLoan(Long loanId) {
        return installmentRepository.findByLoanId(loanId).stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    private InstallmentDto mapToDto(Installment i) {
        InstallmentDto dto = new InstallmentDto();
        dto.setId(i.getId());
        dto.setLoanId(i.getLoanId());
        dto.setDueDate(i.getDueDate());
        dto.setAmount(i.getAmount());
        dto.setStatus(i.getStatus());
        return dto;
    }
}

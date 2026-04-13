package com.loanledger.service;

import com.loanledger.dto.InstallmentDto;
import com.loanledger.entity.Installment;
import com.loanledger.entity.InstallmentStatus;
import com.loanledger.entity.Loan;
import com.loanledger.repository.InstallmentRepository;
import com.loanledger.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstallmentService {
    private final InstallmentRepository installmentRepository;
    private final WalletService walletService;
    private final LoanRepository loanRepository;

    public void generateInstallments(Loan loan, BigDecimal amount, Integer tenure) {
        BigDecimal installmentAmount = amount.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
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
    }

    @Transactional
    public void payInstallment(Long installmentId) {
        Installment installment = installmentRepository.findByIdWithLock(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new RuntimeException("Installment already paid");
        }

        BigDecimal finalAmount = installment.getAmount();
        
        if (LocalDate.now().isAfter(installment.getDueDate())) {
            BigDecimal penalty = finalAmount.multiply(new BigDecimal("0.02"));
            finalAmount = finalAmount.add(penalty);
        }

        Loan loan = loanRepository.findById(installment.getLoanId()).orElseThrow();
        
        walletService.debit(loan.getUserId(), finalAmount, "INST-" + installmentId);

        installment.setStatus(InstallmentStatus.PAID);
        installmentRepository.save(installment);

        loan.setRemainingAmount(loan.getRemainingAmount().subtract(installment.getAmount()));
        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(Loan.LoanStatus.CLOSED);
        }
        loanRepository.save(loan);
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

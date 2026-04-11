package com.loanledger.service;

import com.loanledger.dto.LoanDto;
import com.loanledger.entity.Loan;
import com.loanledger.entity.LoanProduct;
import com.loanledger.repository.LoanProductRepository;
import com.loanledger.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final WalletService walletService;
    private final InstallmentService installmentService;

    public LoanDto applyForLoan(Long userId, Long loanProductId) {
        LoanProduct product = loanProductRepository.findById(loanProductId).orElseThrow();
        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setLoanProductId(loanProductId);
        loan.setRemainingAmount(product.getTotalAmount());
        loan.setStatus(Loan.LoanStatus.PENDING);
        Loan saved = loanRepository.save(loan);
        return mapToDto(saved);
    }

    public void approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow();
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RuntimeException("Can only approve pending loans");
        }
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loanRepository.save(loan);
    }

    @Transactional
    public void disburseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow();
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new RuntimeException("Loan must be APPROVED before disbursement");
        }

        LoanProduct product = loanProductRepository.findById(loan.getLoanProductId()).orElseThrow();
        
        loan.setStatus(Loan.LoanStatus.DISBURSED);
        loanRepository.save(loan);

        walletService.credit(loan.getUserId(), product.getTotalAmount(), "DISB-" + loanId);

        installmentService.generateInstallments(loan, product.getTotalAmount(), product.getTenureMonths());
    }

    public LoanDto getLoan(Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow();
        return mapToDto(loan);
    }
    
    private LoanDto mapToDto(Loan loan) {
        LoanDto dto = new LoanDto();
        dto.setId(loan.getId());
        dto.setUserId(loan.getUserId());
        dto.setLoanProductId(loan.getLoanProductId());
        dto.setRemainingAmount(loan.getRemainingAmount());
        dto.setStatus(loan.getStatus());
        return dto;
    }
}

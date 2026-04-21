package com.loanledger.service;

import com.loanledger.aspect.Audit;
import com.loanledger.dto.LoanDto;
import com.loanledger.entity.Loan;
import com.loanledger.exception.LoanValidationException;
import com.loanledger.exception.ResourceNotFoundException;
import com.loanledger.entity.LoanProduct;
import com.loanledger.events.DisbursalEvent;
import com.loanledger.repository.LoanProductRepository;
import com.loanledger.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import java.math.BigDecimal;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final WalletService walletService;
    private final InstallmentService installmentService;
    private final RiskAssessmentEngine riskAssessmentEngine;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Audit(action = "LOAN_APPLICATION_SUBMITTED")
    public LoanDto applyForLoan(Long userId, Long loanProductId) {
        LoanProduct product = loanProductRepository.findById(loanProductId).orElseThrow(() -> new ResourceNotFoundException("Loan Product not found"));
        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setLoanProductId(loanProductId);
        loan.setRemainingAmount(product.getTotalAmount());
        loan.setStatus(Loan.LoanStatus.PENDING);
        Loan saved = loanRepository.save(loan);
        
        // Trigger asynchronous background credit check!
        riskAssessmentEngine.startAsyncCreditCheck(saved.getId(), userId);
        
        return mapToDto(saved);
    }

    @Audit(action = "LOAN_APPROVED")
    public LoanDto approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new LoanValidationException("Can only approve pending loans");
        }
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loanRepository.save(loan);
        return mapToDto(loan);
    }

    @Transactional
    @Audit(action = "LOAN_DISBURSED")
    public void disburseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new LoanValidationException("Loan must be APPROVED before disbursement");
        }

        LoanProduct product = loanProductRepository.findById(loan.getLoanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan Product not found"));
        
        // 1. Update status to IN_DISBURSAL (Prevents double disbursement)
        loan.setStatus(Loan.LoanStatus.IN_DISBURSAL);
        loanRepository.save(loan);

        // 2. Offload the heavy work (money transfer & schedule gen) to Kafka
        DisbursalEvent event = new DisbursalEvent(
                loan.getId(),
                loan.getUserId(),
                product.getTotalAmount(),
                product.getTenureMonths()
        );
        
        kafkaTemplate.send("disbursal-topic", event);
        // Work will be finished by DisbursalConsumer
    }

    public LoanDto getLoan(Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        return mapToDto(loan);
    }

    @Transactional
    @Audit(action = "LOAN_FORECLOSED")
    public void forecloseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        if (loan.getStatus() != Loan.LoanStatus.DISBURSED) {
            throw new LoanValidationException("Only active disbursed loans can be foreclosed");
        }

        // Logic: Pay only the remaining PRINCIPAL + a 2% foreclosure fee
        BigDecimal outstandingPrincipal = loan.getPrincipalAmount();
        BigDecimal foreclosureFee = outstandingPrincipal.multiply(new BigDecimal("0.02")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalForeclosureAmount = outstandingPrincipal.add(foreclosureFee);

        // 1. Debit the wallet
        walletService.debit(loan.getUserId(), totalForeclosureAmount, "FORECLOSE-" + loanId);

        // 2. Cancel all future installments
        installmentService.cancelPendingInstallments(loanId);

        // 3. Mark loan as closed
        loan.setStatus(Loan.LoanStatus.CLOSED);
        loan.setRemainingAmount(BigDecimal.ZERO);
        loan.setPrincipalAmount(BigDecimal.ZERO);
        loanRepository.save(loan);
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

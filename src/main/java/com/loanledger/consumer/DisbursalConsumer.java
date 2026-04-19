package com.loanledger.consumer;

import com.loanledger.entity.Loan;
import com.loanledger.events.DisbursalEvent;
import com.loanledger.repository.LoanRepository;
import com.loanledger.service.InstallmentService;
import com.loanledger.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisbursalConsumer {

    private final WalletService walletService;
    private final InstallmentService installmentService;
    private final LoanRepository loanRepository;

    @KafkaListener(topics = "disbursal-topic", groupId = "disbursal-group")
    @Transactional
    public void consumeDisbursal(DisbursalEvent event) {
        log.info("💸 [Kafka Consumer] Processing ASYNC Disbursal for Loan ID: {}", event.getLoanId());

        try {
            // 1. Fetch the loan
            Loan loan = loanRepository.findById(event.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Loan not found for async disbursal"));

            // 2. Perform the Wallet Credit (The money part)
            walletService.credit(event.getUserId(), event.getAmount(), "DISB-" + event.getLoanId());

            // 3. Generate Installments (The schedule part)
            installmentService.generateInstallments(loan, event.getAmount(), event.getTenure());

            // 4. Update status to final DISBURSED
            loan.setStatus(Loan.LoanStatus.DISBURSED);
            loanRepository.save(loan);

            log.info("✅ Async Disbursal COMPLETED for Loan ID: {}", event.getLoanId());

        } catch (Exception e) {
            log.error("💥 FAILED Async Disbursal for Loan ID: {}. Error: {}", event.getLoanId(), e.getMessage());
            // Here, you would normally send to a DLQ (Dead Letter Queue)
        }
    }
}

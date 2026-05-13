package com.loanledger.service;
import org.springframework.beans.factory.annotation.Qualifier;
import com.loanledger.entity.Loan;
import com.loanledger.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.loanledger.config.AsyncConfig;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A highly scalable risk assessment engine using core Java multithreading.
 * This runs asynchronously so it does not block the HTTP thread processing the loan application.
 */
@Service
// @RequiredArgsConstructor
@Slf4j
public class RiskAssessmentEngine {

    private final LoanRepository loanRepository;
    private final ExecutorService riscExec;
    public RiskAssessmentEngine(@Qualifier("riscExec") ExecutorService riscExec, LoanRepository loanRepository){
        this.riscExec = riscExec;
        this.loanRepository = loanRepository;
    }
   
  
    public void startAsyncCreditCheck(Long loanId, Long userId) {
        log.info("Dispatching asynchronous risk assessment for Loan ID: {} on thread {}", loanId, Thread.currentThread().getName());

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Simulate heavy network computation (like calling an external Credit Bureau API)
                log.info("[Background Worker] Starting risk scan for Loan {}...", loanId);
                Thread.sleep(3000); 

                // 2. Business Logic Simulation: 
                // Let's pretend user IDs ending in '4' fail the credit check for testing purposes.
                boolean isApprovedByMachine = (userId % 10 != 4); 

                // 3. Atomically update the database without interfering with the main thread
                loanRepository.findById(loanId).ifPresent(loan -> {
                    if (isApprovedByMachine) {
                        log.info("✅ Loan {} passed internal risk models! Awaiting admin approval.", loanId);
                        // Status remains PENDING for manual admin review
                    } else {
                        log.warn("❌ Loan {} REJECTED by background risk engine due to poor credit profile.", loanId);
                        loan.setStatus(Loan.LoanStatus.REJECTED);
                        loanRepository.save(loan);
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Risk assessment interrupted for loan {}", loanId);
            } catch (Exception e) {
                log.error("Critical failure in risk engine for loan {}: {}", loanId, e.getMessage());
            }
        }, riscExec);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Risk Assessment Engine thread pool...");
        riscExec.shutdown();
    }
}

package com.loanledger.service;

import com.loanledger.entity.Loan;
import com.loanledger.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentEngine {

    private final LoanRepository loanRepository;

    // Core Java Multithreading! Using a highly tuned custom ThreadPoolExecutor.
    // Core Threads: 5 (always alive, waiting for work)
    // Max Threads: 20 (scales up to handle spikes in loan applications)
    // KeepAlive: 60 seconds (terminates extra threads after exactly 1 minute of inactivity)
    // Queue: ArrayBlockingQueue of 50 (buffers excess applications before spinning up extra threads)
    private final ExecutorService backgroundExecutor = new ThreadPoolExecutor(
            5, 
            20, 
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(50),
            new ThreadPoolExecutor.CallerRunsPolicy() // If we hit Max Threads AND Queue is full, the calling HTTP thread processes it
    );




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
        }, backgroundExecutor);
    }

    // Best practice: gracefully shutdown thread pool when Spring Boot stops
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Risk Assessment Engine thread pool...");
        backgroundExecutor.shutdown();
    }
}

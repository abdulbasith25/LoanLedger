package com.loanledger.controller;

import com.loanledger.service.InstallmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/simulate")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SimulationController {
    private final InstallmentService installmentService;

    @PostMapping("/concurrent-payment")
    public String simulate(@RequestParam Long installmentId) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    installmentService.payInstallment(installmentId);
                } catch (Exception e) {
                    System.out.println("Payment failed: " + e.getMessage());
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        return "Simulated concurrent payments (check logs)";
    }
}

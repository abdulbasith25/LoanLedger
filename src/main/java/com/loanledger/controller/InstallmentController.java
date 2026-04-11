package com.loanledger.controller;

import com.loanledger.dto.InstallmentDto;
import com.loanledger.dto.InstallmentPayRequest;
import com.loanledger.service.InstallmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/installments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InstallmentController {
    private final InstallmentService installmentService;

    @PostMapping("/pay")
    public String payInstallment(@RequestBody InstallmentPayRequest request) {
        installmentService.payInstallment(request.getInstallmentId());
        return "Installment paid successfully";
    }

    @GetMapping("/loan/{loanId}")
    public List<InstallmentDto> getInstallments(@PathVariable Long loanId) {
        return installmentService.getInstallmentsByLoan(loanId);
    }
}

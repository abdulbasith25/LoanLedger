package com.loanledger.controller;

import com.loanledger.dto.LoanApplyRequest;
import com.loanledger.dto.LoanDto;
import com.loanledger.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/apply")
    public LoanDto applyForLoan(@RequestBody LoanApplyRequest request) {
        return loanService.applyForLoan(request.getUserId(), request.getLoanProductId());
    }

    @PostMapping("/{id}/approve")
    public String approveLoan(@PathVariable Long id) {
        loanService.approveLoan(id);
        return "Loan approved";
    }

    @PostMapping("/{id}/disburse")
    public String disburseLoan(@PathVariable Long id) {
        loanService.disburseLoan(id);
        return "Loan disbursed";
    }

    @GetMapping("/{id}")
    public LoanDto getLoan(@PathVariable Long id) {
        return loanService.getLoan(id);
    }

    @PostMapping("/{id}/foreclose")
    public String forecloseLoan(@PathVariable Long id) {
        loanService.forecloseLoan(id);
        return "Loan foreclosed successfully";
    }
}

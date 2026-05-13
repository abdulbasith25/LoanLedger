package com.loanledger.controller;

import com.loanledger.dto.LoanApplyRequest;
import com.loanledger.dto.LoanDto;
import com.loanledger.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "Endpoints for applying, approving, and managing loans")
public class LoanController {
    private final LoanService loanService;

    @Operation(summary = "Apply for a new loan", description = "Initiates a loan application for a user with a specific loan product.")
    @ApiResponse(responseCode = "200", description = "Loan application submitted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid loan product or user details")
    @PostMapping("/apply")
    public ResponseEntity<LoanDto> applyForLoan(@RequestBody LoanApplyRequest request) {
        log.info("Loan application received for user: {}", request.getUserId());
        LoanDto loan = loanService.applyForLoan(request.getUserId(), request.getLoanProductId());
        return ResponseEntity.ok(loan);
    }

    @Operation(summary = "Approve a loan", description = "Changes the status of a loan to APPROVED. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "200", description = "Loan status updated successfully")
    @ApiResponse(responseCode = "404", description = "Loan ID not found")
    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveLoan(@PathVariable Long id) {
        log.info("Approving loan ID: {}", id);
        loanService.approveLoan(id);
        return ResponseEntity.ok("Loan approved");
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

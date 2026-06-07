package com.loanledger.service.pipeline;

import com.loanledger.entity.Loan;
import com.loanledger.exception.LoanValidationException;
import com.loanledger.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DuplicateLoanValidationHandler extends AbstractLoanValidationHandler {
    private final LoanRepository loanRepository;

    @Override
    public void handle(LoanValidationContext context) {
        List<Loan> userLoans = loanRepository.findByUserId(context.getUserId());
        for (Loan loan : userLoans) {
            if (loan.getStatus() == Loan.LoanStatus.PENDING ||
                loan.getStatus() == Loan.LoanStatus.APPROVED ||
                loan.getStatus() == Loan.LoanStatus.IN_DISBURSAL ||
                loan.getStatus() == Loan.LoanStatus.DISBURSED) {
                throw new LoanValidationException("User already has an active or pending loan application");
            }
        }
        executeNext(context);
    }
}

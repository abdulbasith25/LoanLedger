package com.loanledger.service.pipeline;

import com.loanledger.entity.LoanProduct;
import com.loanledger.exception.ResourceNotFoundException;
import com.loanledger.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanProductValidationHandler extends AbstractLoanValidationHandler {
    private final LoanProductRepository loanProductRepository;

    @Override
    public void handle(LoanValidationContext context) {
        LoanProduct product = loanProductRepository.findById(context.getLoanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan Product not found"));
        context.setLoanProduct(product);
        executeNext(context);
    }
}

package com.loanledger.service.pipeline;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanValidationPipeline {
    private final UserValidationHandler userValidationHandler;
    private final LoanProductValidationHandler loanProductValidationHandler;
    private final DuplicateLoanValidationHandler duplicateLoanValidationHandler;

    private LoanValidationHandler pipelineChain;

    @PostConstruct
    public void init() {
        // Chain the handlers together
        // 1. User existence validation
        // 2. Loan product existence validation
        // 3. Duplicate application validation
        userValidationHandler.setNext(loanProductValidationHandler);
        loanProductValidationHandler.setNext(duplicateLoanValidationHandler);
        
        this.pipelineChain = userValidationHandler;
    }

    public LoanValidationContext validate(Long userId, Long loanProductId) {
        LoanValidationContext context = new LoanValidationContext(userId, loanProductId);
        if (pipelineChain != null) {
            pipelineChain.handle(context);
        }
        return context;
    }
}

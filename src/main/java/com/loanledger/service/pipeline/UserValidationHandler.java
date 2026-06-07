package com.loanledger.service.pipeline;

import com.loanledger.exception.UserNotFoundExcpetion;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidationHandler extends AbstractLoanValidationHandler {
    private final UserRepository userRepository;

    @Override
    public void handle(LoanValidationContext context) {
        if (!userRepository.existsById(context.getUserId())) {
            throw new UserNotFoundExcpetion("User not found");
        }
        executeNext(context);
    }
}

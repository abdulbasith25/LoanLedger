package com.loanledger.service;

import com.loanledger.entity.User;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void incrementScore(Long userId, int scoreToAdd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        int newScore = user.getScore() + scoreToAdd;
        
        user.setScore(newScore);
        userRepository.save(user);
    }
}

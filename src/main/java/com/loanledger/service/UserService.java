package com.loanledger.service;

import com.loanledger.entity.User;
import com.loanledger.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Transactional
    public void incrementScore(Long userId, int scoreToAdd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        int newScore = user.getScore() + scoreToAdd;
        
        user.setScore(newScore);
    }


    public Long getUserIdByUsername(String username){
        User user = userRepository.findByUsername(username). orElseThrow(()-> new UserNotFoundExcpetion("User not found"));
        Long id = user.getId();
        return id;
    }
}

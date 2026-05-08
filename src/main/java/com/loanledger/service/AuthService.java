package com.loanledger.service;
import com.loanledger.dto.AuthResponse;
import com.loanledger.entity.User;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.loanledger.config.JwtUtils;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthResponse register(User user){
       Optional<User> existinguser = userRepository.findByUsername(user.getUsername());
       if(existinguser.isPresent()){
        throw new RuntimeException("UserAlready Esist");
       }
       user.setPassword(passwordEncoder.encode(user.getPassword()));
       user.setRole(User.Role.USER);
       User savedUser = userRepository.save(user);
   
        String token = jwtUtils.generateToken(savedUser);
        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole().name()); 
      
       
    }
    
}
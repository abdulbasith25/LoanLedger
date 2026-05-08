package com.loanledger.controller;

import com.loanledger.config.JwtUtils;
import com.loanledger.config.UserDetailsServiceImpl;
import com.loanledger.dto.AuthRequest;
import com.loanledger.dto.AuthResponse;
import com.loanledger.entity.User;
import com.loanledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        AuthResponse register = AuthService.register(user);

        return ResponseEntity.ok(register);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtUtils.generateToken(userDetails);
        
        User user = userRepository.findByUsername(request.getUsername()).get();
        
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }
}

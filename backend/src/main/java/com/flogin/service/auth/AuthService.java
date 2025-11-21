package com.flogin.service.auth;

import com.flogin.dto.auth.LoginRequest;
import com.flogin.dto.auth.RegisterRequest;
import com.flogin.entity.auth.User;
import com.flogin.repository.auth.UserRepository;
import com.flogin.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    @Transactional
    public User register(RegisterRequest request) {
        // Trim inputs first
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getFullName() != null) {
            request.setFullName(request.getFullName().trim());
        }
        
        // Validate inputs
        validateRegisterRequest(request);
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        // Trim and normalize email first
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().trim().toLowerCase());
        }
        
        // Validate inputs
        validateLoginRequest(request);
        
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        
        return user;
    }
    
    private void validateRegisterRequest(RegisterRequest request) {
        // Email validation
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new BadRequestException("Invalid email format");
        }
        
        // Password validation (6-100 characters, must have letters and numbers)
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        if (request.getPassword().length() < 6 || request.getPassword().length() > 100) {
            throw new BadRequestException("Password must be between 6 and 100 characters");
        }
        if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new BadRequestException("Password must contain both letters and numbers");
        }
        
        // Username validation (3-50 characters, only a-z, A-Z, 0-9, -, ., _)
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        String trimmedName = request.getFullName().trim();
        if (trimmedName.length() < 3 || trimmedName.length() > 50) {
            throw new BadRequestException("Username must be between 3 and 50 characters");
        }
        if (!trimmedName.matches("^[a-zA-Z0-9._-]+$")) {
            throw new BadRequestException("Username can only contain letters, numbers, dots, hyphens, and underscores");
        }
    }
    
    private void validateLoginRequest(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
    }
}

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

/**
 * Authentication Service
 * 
 * Validation Rules theo Assignment:
 * - Username: 3-50 ký tự, chỉ chứa a-z, A-Z, 0-9, -, ., _
 * - Password: 6-100 ký tự, phải có cả chữ VÀ số
 * - Email: Định dạng email hợp lệ
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Username: 3-50 ký tự, chỉ chứa a-z, A-Z, 0-9, -, ., _
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");
    
    // Email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Password phải có ít nhất 1 chữ cái
    private static final Pattern PASSWORD_LETTER_PATTERN = Pattern.compile(".*[a-zA-Z].*");
    
    // Password phải có ít nhất 1 số
    private static final Pattern PASSWORD_NUMBER_PATTERN = Pattern.compile(".*[0-9].*");
    
    @Transactional
    public User register(RegisterRequest request) {
        // Trim inputs first
        if (request.getUsername() != null) {
            request.setUsername(request.getUsername().trim());
        }
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getFullName() != null) {
            request.setFullName(request.getFullName().trim());
        }
        
        // Validate inputs
        validateRegisterRequest(request);
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        // Trim username
        if (request.getUsername() != null) {
            request.setUsername(request.getUsername().trim());
        }
        
        // Validate inputs
        validateLoginRequest(request);
        
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadRequestException("Invalid username or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }
        
        return user;
    }
    
    /**
     * Validate username theo quy tắc assignment:
     * - 3-50 ký tự
     * - Chỉ chứa a-z, A-Z, 0-9, -, ., _
     */
    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        
        String trimmedUsername = username.trim();
        
        if (trimmedUsername.length() < 3) {
            throw new BadRequestException("Username must be at least 3 characters");
        }
        
        if (trimmedUsername.length() > 50) {
            throw new BadRequestException("Username must not exceed 50 characters");
        }
        
        if (!USERNAME_PATTERN.matcher(trimmedUsername).matches()) {
            throw new BadRequestException("Username can only contain letters, numbers, dots, hyphens, and underscores");
        }
    }
    
    /**
     * Validate password theo quy tắc assignment:
     * - 6-100 ký tự
     * - Phải có cả chữ VÀ số
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        
        if (password.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
        
        if (password.length() > 100) {
            throw new BadRequestException("Password must not exceed 100 characters");
        }
        
        if (!PASSWORD_LETTER_PATTERN.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one letter");
        }
        
        if (!PASSWORD_NUMBER_PATTERN.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one number");
        }
    }
    
    private void validateRegisterRequest(RegisterRequest request) {
        // Validate username
        validateUsername(request.getUsername());
        
        // Validate email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new BadRequestException("Invalid email format");
        }
        
        // Validate password
        validatePassword(request.getPassword());
        
        // Validate fullName
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new BadRequestException("Full name is required");
        }
    }
    
    private void validateLoginRequest(LoginRequest request) {
        // Validate username
        validateUsername(request.getUsername());
        
        // Validate password (chỉ check required, không check rules khi login)
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
    }
}

package com.flogin.service.auth;

import com.flogin.dto.auth.LoginRequest;
import com.flogin.dto.auth.RegisterRequest;
import com.flogin.entity.auth.User;
import com.flogin.exception.BadRequestException;
import com.flogin.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Register new user
     */
    public User register(RegisterRequest request) {
        // Validate inputs
        validateRegisterRequest(request);

        // Trim inputs
        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(fullName);

        // Save and return
        return userRepository.save(user);
    }

    /**
     * Login user
     */
    public User login(LoginRequest request) {
        // Validate inputs
        validateLoginRequest(request);

        // Trim and lowercase email
        String email = request.getEmail().trim().toLowerCase();

        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        return user;
    }

    /**
     * Validate register request
     */
    private void validateRegisterRequest(RegisterRequest request) {
        // Check null
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new BadRequestException("Full name is required");
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            throw new BadRequestException("Invalid email format");
        }

        // Validate password length
        if (request.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
    }

    /**
     * Validate login request
     */
    private void validateLoginRequest(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
    }
}
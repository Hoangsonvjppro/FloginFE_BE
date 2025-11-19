package com.flogin.service.auth;

import com.flogin.dto.auth.LoginRequest;
import com.flogin.dto.auth.RegisterRequest;
import com.flogin.entity.auth.User;
import com.flogin.repository.auth.UserRepository;
import com.flogin.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;  // ← THÊM DÒNG NÀY

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("Password123!");
        validRegisterRequest.setFullName("Test User");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("Password123!");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("hashedPassword");
        mockUser.setFullName("Test User");
    }

    // ============= REGISTER TESTS =============

    @Test
    @DisplayName("Should register user successfully with valid data")
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");  // ← THÊM
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User result = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when email already exists")
    void testRegister_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);  // ← SỬA DÒNG NÀY

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(validRegisterRequest)
        );
        
        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when email is invalid")
    void testRegister_InvalidEmail_ThrowsException() {
        // Arrange
        validRegisterRequest.setEmail("invalid-email");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(validRegisterRequest)
        );
        
        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when password is too short")
    void testRegister_ShortPassword_ThrowsException() {
        // Arrange
        validRegisterRequest.setPassword("123");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(validRegisterRequest)
        );
        
        assertTrue(exception.getMessage().contains("Password must be at least 8 characters"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when full name is empty")
    void testRegister_EmptyFullName_ThrowsException() {
        // Arrange
        validRegisterRequest.setFullName("");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(validRegisterRequest)
        );
        
        assertTrue(exception.getMessage().contains("Full name is required"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should hash password before saving")
    void testRegister_PasswordIsHashed() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword123456789");  // ← THÊM MOCK
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertNotEquals("Password123!", savedUser.getPassword());
            assertTrue(savedUser.getPassword().length() > 20);
            return savedUser;
        });

        // Act
        authService.register(validRegisterRequest);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ============= LOGIN TESTS =============

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);  // ← THÊM MOCK

        // Act
        User result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw BadRequestException when email not found")
    void testLogin_EmailNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(validLoginRequest)
        );
        
        assertTrue(exception.getMessage().contains("Invalid email or password"));
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw BadRequestException when password is incorrect")
    void testLogin_WrongPassword_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);  // ← THÊM MOCK

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(validLoginRequest)
        );
        
        assertTrue(exception.getMessage().contains("Invalid email or password"));
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw BadRequestException when email is null")
    void testLogin_NullEmail_ThrowsException() {
        // Arrange
        validLoginRequest.setEmail(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(validLoginRequest)
        );
        
        assertTrue(exception.getMessage().contains("Email is required"));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw BadRequestException when password is null")
    void testLogin_NullPassword_ThrowsException() {
        // Arrange
        validLoginRequest.setPassword(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(validLoginRequest)
        );
        
        assertTrue(exception.getMessage().contains("Password is required"));
        verify(userRepository, never()).findByEmail(anyString());
    }

    // ============= EDGE CASES =============

    @Test
    @DisplayName("Should handle special characters in email")
    void testRegister_SpecialCharactersInEmail() {
        // Arrange
        validRegisterRequest.setEmail("test+special@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");  // ← THÊM MOCK
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User result = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should trim whitespace from inputs")
    void testRegister_TrimWhitespace() {
        // Arrange
        validRegisterRequest.setEmail("  test@example.com  ");
        validRegisterRequest.setFullName("  Test User  ");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");  // ← THÊM MOCK
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals("test@example.com", savedUser.getEmail());
            assertEquals("Test User", savedUser.getFullName());
            return savedUser;
        });

        // Act
        authService.register(validRegisterRequest);

        // Assert
    }

    @Test
    @DisplayName("Should be case-insensitive for email during login")
    void testLogin_CaseInsensitiveEmail() {
        // Arrange
        validLoginRequest.setEmail("TEST@EXAMPLE.COM");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);  // ← THÊM MOCK

        // Act
        User result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(anyString());
    }
}
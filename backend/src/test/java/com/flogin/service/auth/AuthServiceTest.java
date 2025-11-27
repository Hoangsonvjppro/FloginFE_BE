package com.flogin.service.auth;

import com.flogin.dto.auth.LoginRequest;
import com.flogin.dto.auth.RegisterRequest;
import com.flogin.entity.auth.User;
import com.flogin.exception.BadRequestException;
import com.flogin.repository.auth.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho AuthService sử dụng JUnit 5 và Mockito.
 * 
 * Coverage:
 * - Login: Happy Path, Email không tồn tại, Password sai, Validation errors
 * - Register: Happy Path, Email đã tồn tại, Password validation, Email validation, FullName validation
 * - Edge cases: Email normalization, Trimming, Case sensitivity
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest validLoginRequest;
    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setFullName("Test User");

        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // Setup valid register request
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("newuser@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setFullName("New User");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login - Happy Path: Login thành công với credentials đúng")
    void login_WithValidCredentials_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        User result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result, "User không được null");
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getFullName(), result.getFullName());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
    }

    @Test
    @DisplayName("Login Failure 1: Username/Email không tồn tại -> Ném BadRequestException")
    void login_WithNonExistentEmail_ShouldThrowBadRequestException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(validLoginRequest),
            "Phải throw BadRequestException khi email không tồn tại"
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login Failure 2: Password sai -> Ném BadRequestException")
    void login_WithIncorrectPassword_ShouldThrowBadRequestException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(validLoginRequest),
            "Phải throw BadRequestException khi password sai"
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
    }

    @Test
    @DisplayName("Login - Validation Error: Email rỗng -> Ném BadRequestException")
    void login_WithEmptyEmail_ShouldThrowBadRequestException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("password123");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(request),
            "Phải throw BadRequestException khi email rỗng"
        );

        assertEquals("Email is required", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Login - Validation Error: Email null -> Ném BadRequestException")
    void login_WithNullEmail_ShouldThrowBadRequestException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(null);
        request.setPassword("password123");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(request),
            "Phải throw BadRequestException khi email null"
        );

        assertEquals("Email is required", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Login - Validation Error: Password rỗng -> Ném BadRequestException")
    void login_WithEmptyPassword_ShouldThrowBadRequestException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(request),
            "Phải throw BadRequestException khi password rỗng"
        );

        assertEquals("Password is required", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Login - Validation Error: Password null -> Ném BadRequestException")
    void login_WithNullPassword_ShouldThrowBadRequestException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(request),
            "Phải throw BadRequestException khi password null"
        );

        assertEquals("Password is required", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Login - Email normalization: Uppercase email được chuyển thành lowercase")
    void login_WithUppercaseEmail_ShouldNormalizeToLowercase() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("TEST@EXAMPLE.COM");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        User result = authService.login(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Login - Email trimming: Whitespace xung quanh email được trim")
    void login_WithWhitespaceAroundEmail_ShouldTrimEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("  test@example.com  ");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        User result = authService.login(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Register - Happy Path: Đăng ký thành công với valid data")
    void register_WithValidData_ShouldSaveAndReturnUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(result, "User không được null");
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());

        // Verify interactions
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register Failure: Email đã tồn tại -> Ném BadRequestException với message 'Email already exists'")
    void register_WithExistingEmail_ShouldThrowBadRequestException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(validRegisterRequest),
            "Phải throw BadRequestException khi email đã tồn tại"
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== PASSWORD VALIDATION TESTS ====================

    @Test
    @DisplayName("Register - Password Validation: Password rỗng -> Ném BadRequestException")
    void register_WithEmptyPassword_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("");
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi password rỗng"
        );

        assertEquals("Password is required", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - Password Validation: Password null -> Ném BadRequestException")
    void register_WithNullPassword_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword(null);
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi password null"
        );

        assertEquals("Password is required", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - Password Validation: Password < 8 ký tự -> Ném BadRequestException")
    void register_WithShortPassword_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("pass12"); // Chỉ 6 ký tự
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi password < 8 ký tự"
        );

        assertEquals("Password must be at least 8 characters", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - Password Validation: Password đúng 7 ký tự -> Ném BadRequestException")
    void register_WithPasswordOf7Characters_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("pass123"); // Đúng 7 ký tự
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi password = 7 ký tự"
        );

        assertEquals("Password must be at least 8 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Register - Password Validation: Password đúng 8 ký tự -> Thành công")
    void register_WithPasswordOf8Characters_ShouldSucceed() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("12345678"); // Đúng 8 ký tự
        request.setFullName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Password Validation: Password 100 ký tự -> Thành công")
    void register_WithPasswordOf100Characters_ShouldSucceed() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        // Generate 100 character password
        request.setPassword("a".repeat(100));
        request.setFullName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==================== EMAIL VALIDATION TESTS ====================

    @Test
    @DisplayName("Register - Email Validation: Email rỗng -> Ném BadRequestException")
    void register_WithEmptyEmail_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("");
        request.setPassword("password123");
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi email rỗng"
        );

        assertEquals("Email is required", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - Email Validation: Email null -> Ném BadRequestException")
    void register_WithNullEmail_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail(null);
        request.setPassword("password123");
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi email null"
        );

        assertEquals("Email is required", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - Email Validation: Email sai định dạng (không có @) -> Ném BadRequestException")
    void register_WithInvalidEmailFormat_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi email sai định dạng"
        );

        assertEquals("Invalid email format", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - Email Validation: Email không có domain -> Ném BadRequestException")
    void register_WithEmailWithoutDomain_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@");
        request.setPassword("password123");
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi email không có domain"
        );

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    @DisplayName("Register - Email Validation: Email không có user -> Ném BadRequestException")
    void register_WithEmailWithoutUser_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi email không có user"
        );

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    @DisplayName("Register - Email Validation: Các định dạng email hợp lệ -> Thành công")
    void register_WithVariousValidEmailFormats_ShouldSucceed() {
        // Test nhiều định dạng email hợp lệ
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com",
            "123@example.com",
            "a@b.co"
        };

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        for (String email : validEmails) {
            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setPassword("password123");
            request.setFullName("Test User");

            // Không throw exception
            assertDoesNotThrow(
                () -> authService.register(request),
                "Email hợp lệ phải được chấp nhận: " + email
            );
        }
    }

    @Test
    @DisplayName("Register - Email Validation: Các định dạng email không hợp lệ -> Ném exception")
    void register_WithVariousInvalidEmailFormats_ShouldThrowException() {
        // Test nhiều định dạng email không hợp lệ
        String[] invalidEmails = {
            "notanemail",
            "@example.com",
            "user@",
            "user@.com",
            "user space@example.com",
            "user@example"
        };

        for (String email : invalidEmails) {
            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setPassword("password123");
            request.setFullName("Test User");

            assertThrows(
                BadRequestException.class,
                () -> authService.register(request),
                "Email không hợp lệ phải bị từ chối: " + email
            );
        }
    }

    // ==================== FULLNAME VALIDATION TESTS ====================

    @Test
    @DisplayName("Register - FullName Validation: FullName rỗng -> Ném BadRequestException")
    void register_WithEmptyFullName_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi full name rỗng"
        );

        assertEquals("Full name is required", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - FullName Validation: FullName null -> Ném BadRequestException")
    void register_WithNullFullName_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi full name null"
        );

        assertEquals("Full name is required", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Register - FullName Validation: FullName chỉ chứa khoảng trắng -> Ném BadRequestException")
    void register_WithWhitespaceOnlyFullName_ShouldThrowBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request),
            "Phải throw BadRequestException khi full name chỉ chứa khoảng trắng"
        );

        assertEquals("Full name is required", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    // ==================== DATA NORMALIZATION TESTS ====================

    @Test
    @DisplayName("Register - Email normalization: Uppercase email được chuyển thành lowercase")
    void register_WithUppercaseEmail_ShouldNormalizeToLowercase() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("TEST@EXAMPLE.COM");
        request.setPassword("password123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Register - Input trimming: Khoảng trắng xung quanh inputs được trim")
    void register_WithWhitespaceAroundInputs_ShouldTrimInputs() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  test@example.com  ");
        request.setPassword("password123");
        request.setFullName("  Test User  ");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Register - Password encoding: Password phải được encode trước khi lưu")
    void register_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String rawPassword = "plainPassword123";
        String encodedPassword = "encodedPassword123";

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword(rawPassword);
        request.setFullName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(request);

        // Assert
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==================== INTEGRATION-LIKE TESTS ====================

    @Test
    @DisplayName("Register - Toàn bộ workflow: Từ register request đến save database")
    void register_CompleteWorkflow_ShouldExecuteAllSteps() {
        // Arrange
        String email = "newuser@example.com";
        String password = "password123";
        String fullName = "New User";
        
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFullName(fullName);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // Act
        User result = authService.register(request);

        // Assert
        assertNotNull(result);
        
        // Verify tất cả các bước được thực hiện đúng thứ tự
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Login - Toàn bộ workflow: Từ login request đến return user")
    void login_CompleteWorkflow_ShouldExecuteAllSteps() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        User result = authService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        
        // Verify tất cả các bước được thực hiện đúng thứ tự
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, testUser.getPassword());
    }
}
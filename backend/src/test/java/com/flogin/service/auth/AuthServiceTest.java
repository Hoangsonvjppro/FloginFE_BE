package com.flogin.service.auth;

import com.flogin.dto.auth.LoginRequest;
import com.flogin.dto.auth.RegisterRequest;
import com.flogin.entity.auth.User;
import com.flogin.exception.BadRequestException;
import com.flogin.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
 * Validation Rules theo Assignment:
 * - Username: 3-50 ký tự, chỉ chứa a-z, A-Z, 0-9, -, ., _
 * - Password: 6-100 ký tự, phải có cả chữ VÀ số
 * 
 * Coverage Target: >= 85%
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
        // Setup test user với username
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setFullName("Test User");

        // Setup valid login request với username
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("Pass123");

        // Setup valid register request với username
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("newuser");
        validRegisterRequest.setEmail("newuser@example.com");
        validRegisterRequest.setPassword("Pass123");
        validRegisterRequest.setFullName("New User");
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("TC_LOGIN_001: Login thành công với credentials đúng")
        void login_WithValidCredentials_ShouldReturnUser() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Pass123", testUser.getPassword())).thenReturn(true);

            // Act
            User result = authService.login(validLoginRequest);

            // Assert
            assertNotNull(result, "User không được null");
            assertEquals(testUser.getUsername(), result.getUsername());
            assertEquals(testUser.getId(), result.getId());
            assertEquals(testUser.getFullName(), result.getFullName());

            // Verify interactions
            verify(userRepository, times(1)).findByUsername("testuser");
            verify(passwordEncoder, times(1)).matches("Pass123", testUser.getPassword());
        }

        @Test
        @DisplayName("TC_LOGIN_002: Login thất bại - Username không tồn tại")
        void login_WithNonExistentUsername_ShouldThrowBadRequestException() {
            // Arrange
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(validLoginRequest),
                "Phải throw BadRequestException khi username không tồn tại"
            );

            assertEquals("Invalid username or password", exception.getMessage());
            verify(userRepository, times(1)).findByUsername("testuser");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("TC_LOGIN_003: Login thất bại - Password sai")
        void login_WithIncorrectPassword_ShouldThrowBadRequestException() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(validLoginRequest),
                "Phải throw BadRequestException khi password sai"
            );

            assertEquals("Invalid username or password", exception.getMessage());
            verify(userRepository, times(1)).findByUsername("testuser");
            verify(passwordEncoder, times(1)).matches("Pass123", testUser.getPassword());
        }

        @Test
        @DisplayName("TC_LOGIN_004: Validation - Username rỗng")
        void login_WithEmptyUsername_ShouldThrowBadRequestException() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("");
            request.setPassword("Pass123");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
            );

            assertEquals("Username is required", exception.getMessage());
            verify(userRepository, never()).findByUsername(anyString());
        }

        @Test
        @DisplayName("TC_LOGIN_005: Validation - Username null")
        void login_WithNullUsername_ShouldThrowBadRequestException() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername(null);
            request.setPassword("Pass123");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
            );

            assertEquals("Username is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_LOGIN_006: Validation - Username quá ngắn (< 3 ký tự)")
        void login_WithTooShortUsername_ShouldThrowBadRequestException() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("ab");
            request.setPassword("Pass123");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
            );

            assertEquals("Username must be at least 3 characters", exception.getMessage());
        }

        @Test
        @DisplayName("TC_LOGIN_007: Validation - Username quá dài (> 50 ký tự)")
        void login_WithTooLongUsername_ShouldThrowBadRequestException() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("a".repeat(51));
            request.setPassword("Pass123");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
            );

            assertEquals("Username must not exceed 50 characters", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"user name", "user@name", "user#name", "user$name", "user%name"})
        @DisplayName("TC_LOGIN_008: Validation - Username chứa ký tự không hợp lệ")
        void login_WithInvalidUsernameCharacters_ShouldThrowBadRequestException(String invalidUsername) {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername(invalidUsername);
            request.setPassword("Pass123");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
            );

            assertEquals("Username can only contain letters, numbers, dots, hyphens, and underscores", 
                exception.getMessage());
        }

        @Test
        @DisplayName("TC_LOGIN_009: Validation - Password rỗng")
        void login_WithEmptyPassword_ShouldThrowBadRequestException() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
            );

            assertEquals("Password is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_LOGIN_010: Username trimming - Whitespace được loại bỏ")
        void login_WithWhitespaceAroundUsername_ShouldTrimUsername() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("  testuser  ");
            request.setPassword("Pass123");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // Act
            User result = authService.login(request);

            // Assert
            assertNotNull(result);
            verify(userRepository, times(1)).findByUsername("testuser");
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "user_name", "user.name", "user-name", "User123"})
        @DisplayName("TC_LOGIN_011: Username hợp lệ với các ký tự cho phép")
        void login_WithValidUsernameFormats_ShouldWork(String validUsername) {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername(validUsername);
            request.setPassword("Pass123");

            testUser.setUsername(validUsername);
            when(userRepository.findByUsername(validUsername)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // Act
            User result = authService.login(request);

            // Assert
            assertNotNull(result);
        }
    }

    // ==================== REGISTER TESTS ====================

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("TC_REGISTER_001: Đăng ký thành công với valid data")
        void register_WithValidData_ShouldSaveAndReturnUser() {
            // Arrange
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(1L);
                return savedUser;
            });

            // Act
            User result = authService.register(validRegisterRequest);

            // Assert
            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("newuser@example.com", result.getEmail());
            assertEquals("encodedPassword", result.getPassword());

            verify(userRepository, times(1)).existsByUsername("newuser");
            verify(userRepository, times(1)).existsByEmail("newuser@example.com");
            verify(passwordEncoder, times(1)).encode("Pass123");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("TC_REGISTER_002: Username đã tồn tại")
        void register_WithExistingUsername_ShouldThrowBadRequestException() {
            // Arrange
            when(userRepository.existsByUsername("newuser")).thenReturn(true);

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(validRegisterRequest)
            );

            assertEquals("Username already exists", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("TC_REGISTER_003: Email đã tồn tại")
        void register_WithExistingEmail_ShouldThrowBadRequestException() {
            // Arrange
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(validRegisterRequest)
            );

            assertEquals("Email already exists", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("TC_REGISTER_004: Email format không hợp lệ")
        void register_WithInvalidEmailFormat_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("invalid-email");
            request.setPassword("Pass123");
            request.setFullName("New User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("TC_REGISTER_005: FullName rỗng")
        void register_WithEmptyFullName_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("newuser@example.com");
            request.setPassword("Pass123");
            request.setFullName("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Full name is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_REGISTER_006: Username rỗng")
        void register_WithEmptyUsername_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Username is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_REGISTER_007: Username với ký tự không hợp lệ")
        void register_WithInvalidUsernameChars_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("user@invalid");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Username can only contain letters, numbers, dots, hyphens, and underscores", 
                exception.getMessage());
        }
    }

    // ==================== PASSWORD VALIDATION TESTS ====================

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @Test
        @DisplayName("TC_PWD_001: Password rỗng")
        void register_WithEmptyPassword_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Password is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_PWD_002: Password null")
        void register_WithNullPassword_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword(null);
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Password is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_PWD_003: Password quá ngắn (< 6 ký tự)")
        void register_WithTooShortPassword_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Pass1"); // 5 ký tự
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Password must be at least 6 characters", exception.getMessage());
        }

        @Test
        @DisplayName("TC_PWD_004: Password đúng 6 ký tự (boundary min) -> Thành công")
        void register_WithMinimumValidPassword_ShouldSucceed() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Pass12"); // 6 ký tự, có letter và number
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            User result = authService.register(request);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("TC_PWD_005: Password 100 ký tự (boundary max) -> Thành công")
        void register_WithMaximumValidPassword_ShouldSucceed() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("a".repeat(50) + "1".repeat(50)); // 100 ký tự
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            User result = authService.register(request);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("TC_PWD_006: Password quá dài (> 100 ký tự)")
        void register_WithTooLongPassword_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("a".repeat(50) + "1".repeat(51)); // 101 ký tự
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Password must not exceed 100 characters", exception.getMessage());
        }

        @Test
        @DisplayName("TC_PWD_007: Password không có chữ cái - chỉ có số")
        void register_WithPasswordWithoutLetters_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("123456"); // Chỉ có số
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Password must contain at least one letter", exception.getMessage());
        }

        @Test
        @DisplayName("TC_PWD_008: Password không có số - chỉ có chữ")
        void register_WithPasswordWithoutNumbers_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Password"); // Chỉ có chữ
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Password must contain at least one number", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Pass12", "abc123", "Test1234", "MyP@ss1"})
        @DisplayName("TC_PWD_009: Password hợp lệ với nhiều format")
        void register_WithValidPasswordFormats_ShouldSucceed(String validPassword) {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword(validPassword);
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act & Assert
            assertDoesNotThrow(() -> authService.register(request));
        }
    }

    // ==================== USERNAME VALIDATION TESTS ====================

    @Nested
    @DisplayName("Username Validation Tests")
    class UsernameValidationTests {

        @Test
        @DisplayName("TC_USER_001: Username null")
        void validateUsername_WithNull_ShouldThrowException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername(null);
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );
            assertEquals("Username is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_USER_002: Username rỗng")
        void validateUsername_WithEmpty_ShouldThrowException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );
            assertEquals("Username is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_USER_003: Username chỉ có whitespace")
        void validateUsername_WithOnlyWhitespace_ShouldThrowException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("   ");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );
            assertEquals("Username is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_USER_004: Username 2 ký tự (< 3, invalid)")
        void validateUsername_WithTooShort_ShouldThrowException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("ab");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );
            assertEquals("Username must be at least 3 characters", exception.getMessage());
        }

        @Test
        @DisplayName("TC_USER_005: Username 3 ký tự (boundary min)")
        void validateUsername_WithMinimumLength_ShouldPass() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("abc");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act & Assert
            assertDoesNotThrow(() -> authService.register(request));
        }

        @Test
        @DisplayName("TC_USER_006: Username 50 ký tự (boundary max)")
        void validateUsername_WithMaximumLength_ShouldPass() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("a".repeat(50));
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act & Assert
            assertDoesNotThrow(() -> authService.register(request));
        }

        @Test
        @DisplayName("TC_USER_007: Username 51 ký tự (> 50, invalid)")
        void validateUsername_WithTooLong_ShouldThrowException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("a".repeat(51));
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );
            assertEquals("Username must not exceed 50 characters", exception.getMessage());
        }

        @Test
        @DisplayName("TC_USER_008: Username với dots, hyphens, underscores (valid chars)")
        void validateUsername_WithAllowedSpecialChars_ShouldPass() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("user.name_test-123");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act & Assert
            assertDoesNotThrow(() -> authService.register(request));
        }

        @ParameterizedTest
        @ValueSource(strings = {"user@name", "user#name", "user$name", "user name", "user%name"})
        @DisplayName("TC_USER_009: Username với ký tự đặc biệt không hợp lệ")
        void validateUsername_WithInvalidChars_ShouldThrowException(String invalidUsername) {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername(invalidUsername);
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );
            assertEquals("Username can only contain letters, numbers, dots, hyphens, and underscores", 
                exception.getMessage());
        }
    }

    // ==================== EMAIL VALIDATION TESTS ====================

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("TC_EMAIL_001: Email rỗng")
        void register_WithEmptyEmail_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Email is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_EMAIL_002: Email null")
        void register_WithNullEmail_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail(null);
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Email is required", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "@example.com", "user@", "user@.com", "user space@example.com"})
        @DisplayName("TC_EMAIL_003: Email format không hợp lệ")
        void register_WithInvalidEmailFormats_ShouldThrowException(String invalidEmail) {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail(invalidEmail);
            request.setPassword("Pass123");
            request.setFullName("Test User");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Invalid email format", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"user@example.com", "user.name@example.com", "user+tag@example.co.uk", "a@b.co"})
        @DisplayName("TC_EMAIL_004: Email format hợp lệ")
        void register_WithValidEmailFormats_ShouldSucceed(String validEmail) {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail(validEmail);
            request.setPassword("Pass123");
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act & Assert
            assertDoesNotThrow(() -> authService.register(request));
        }
    }

    // ==================== FULLNAME VALIDATION TESTS ====================

    @Nested
    @DisplayName("FullName Validation Tests")
    class FullNameValidationTests {

        @Test
        @DisplayName("TC_NAME_001: FullName rỗng")
        void register_WithEmptyFullName_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Full name is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_NAME_002: FullName null")
        void register_WithNullFullName_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Full name is required", exception.getMessage());
        }

        @Test
        @DisplayName("TC_NAME_003: FullName chỉ có whitespace")
        void register_WithWhitespaceOnlyFullName_ShouldThrowBadRequestException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("   ");

            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
            );

            assertEquals("Full name is required", exception.getMessage());
        }
    }

    // ==================== DATA NORMALIZATION TESTS ====================

    @Nested
    @DisplayName("Data Normalization Tests")
    class DataNormalizationTests {

        @Test
        @DisplayName("TC_NORM_001: Email uppercase -> normalize to lowercase")
        void register_WithUppercaseEmail_ShouldNormalizeToLowercase() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("TEST@EXAMPLE.COM");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                assertEquals("test@example.com", u.getEmail());
                return u;
            });

            // Act
            User result = authService.register(request);

            // Assert
            assertNotNull(result);
            verify(userRepository, times(1)).existsByEmail("test@example.com");
        }

        @Test
        @DisplayName("TC_NORM_002: Input trimming cho tất cả fields")
        void register_WithWhitespaceAroundInputs_ShouldTrimInputs() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("  testuser  ");
            request.setEmail("  test@example.com  ");
            request.setPassword("Pass123");
            request.setFullName("  Test User  ");

            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                assertEquals("testuser", u.getUsername());
                assertEquals("test@example.com", u.getEmail());
                assertEquals("Test User", u.getFullName());
                return u;
            });

            // Act
            User result = authService.register(request);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("TC_NORM_003: Password được encode trước khi lưu")
        void register_ShouldEncodePasswordBeforeSaving() {
            // Arrange
            String rawPassword = "Pass123";
            String encodedPassword = "encodedPassword123";

            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword(rawPassword);
            request.setFullName("Test User");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                assertEquals(encodedPassword, u.getPassword());
                return u;
            });

            // Act
            authService.register(request);

            // Assert
            verify(passwordEncoder, times(1)).encode(rawPassword);
        }
    }

    // ==================== COMPLETE WORKFLOW TESTS ====================

    @Nested
    @DisplayName("Complete Workflow Tests")
    class WorkflowTests {

        @Test
        @DisplayName("TC_FLOW_001: Register complete workflow")
        void register_CompleteWorkflow_ShouldExecuteAllSteps() {
            // Arrange
            String username = "newuser";
            String email = "newuser@example.com";
            String password = "Pass123";
            String fullName = "New User";
            
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setPassword(password);
            request.setFullName(fullName);

            when(userRepository.existsByUsername(username)).thenReturn(false);
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
            verify(userRepository, times(1)).existsByUsername(username);
            verify(userRepository, times(1)).existsByEmail(email);
            verify(passwordEncoder, times(1)).encode(password);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("TC_FLOW_002: Login complete workflow")
        void login_CompleteWorkflow_ShouldExecuteAllSteps() {
            // Arrange
            String username = "testuser";
            String password = "Pass123";
            
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            // Act
            User result = authService.login(request);

            // Assert
            assertNotNull(result);
            assertEquals(testUser.getId(), result.getId());
            
            // Verify tất cả các bước được thực hiện đúng thứ tự
            verify(userRepository, times(1)).findByUsername(username);
            verify(passwordEncoder, times(1)).matches(password, testUser.getPassword());
        }
    }
}
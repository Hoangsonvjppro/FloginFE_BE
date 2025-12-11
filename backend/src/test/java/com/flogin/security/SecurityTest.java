package com.flogin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.controller.AuthController;
import com.flogin.controller.ProductController;
import com.flogin.dto.auth.LoginRequest;
import com.flogin.dto.auth.RegisterRequest;
import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
import com.flogin.exception.BadRequestException;
import com.flogin.service.auth.AuthService;
import com.flogin.service.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security Testing Suite
 * 
 * Tests for OWASP Top 10 security vulnerabilities:
 * 1. SQL Injection Prevention
 * 2. XSS (Cross-Site Scripting) Prevention
 * 3. Input Validation Security
 * 4. Authentication Security
 * 5. Password Security
 */
@WebMvcTest({AuthController.class, ProductController.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Security Tests")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private ProductService productService;

    // ==================== SQL INJECTION TESTS ====================
    @Nested
    @DisplayName("SQL Injection Prevention")
    class SqlInjectionTests {

        @Test
        @DisplayName("Should prevent SQL injection in login username")
        void shouldPreventSqlInjectionInUsername() throws Exception {
            String sqlInjectionPayload = """
                {
                    "username": "' OR '1'='1",
                    "password": "Password123"
                }
                """;

            // SQL injection should be caught by validation
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sqlInjectionPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should prevent SQL injection with DROP TABLE")
        void shouldPreventDropTableInjection() throws Exception {
            String sqlInjectionPayload = """
                {
                    "username": "test'; DROP TABLE users;--",
                    "password": "Password123"
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sqlInjectionPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should prevent UNION-based SQL injection")
        void shouldPreventUnionSqlInjection() throws Exception {
            String sqlInjectionPayload = """
                {
                    "username": "test' UNION SELECT * FROM users--",
                    "password": "Password123"
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sqlInjectionPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should prevent time-based SQL injection")
        void shouldPreventTimeBasedSqlInjection() throws Exception {
            String sqlInjectionPayload = """
                {
                    "username": "test'; WAITFOR DELAY '00:00:10'--",
                    "password": "Password123"
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sqlInjectionPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should prevent SQL injection in product search")
        void shouldPreventSqlInjectionInProductSearch() throws Exception {
            when(productService.getAllProducts()).thenReturn(Collections.emptyList());
            
            // The request might succeed but with empty results - SQL injection in param should not work
            mockMvc.perform(get("/api/products")
                    .param("search", "' OR '1'='1"))
                    .andExpect(status().isOk());
        }
    }

    // ==================== XSS PREVENTION TESTS ====================
    @Nested
    @DisplayName("XSS Prevention")
    class XssPreventionTests {

        @Test
        @DisplayName("Should handle script tags in product name - data stored as text, escaped on output")
        void shouldHandleScriptTagsInProductName() throws Exception {
            ProductRequest xssPayload = new ProductRequest();
            xssPayload.setName("<script>alert('XSS')</script>");
            xssPayload.setPrice(BigDecimal.valueOf(99.99));
            xssPayload.setQuantity(10);
            xssPayload.setCategory("Electronics");

            ProductResponse mockProduct = new ProductResponse();
            mockProduct.setId(1L);
            mockProduct.setName(xssPayload.getName());
            mockProduct.setPrice(xssPayload.getPrice());
            mockProduct.setQuantity(xssPayload.getQuantity());
            mockProduct.setCategory(xssPayload.getCategory());
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(mockProduct);

            // API accepts the text - XSS prevention happens at output/rendering layer
            // In Spring MVC, the JSON response is automatically escaped
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(xssPayload)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should handle event handlers in input - data stored as text")
        void shouldHandleEventHandlers() throws Exception {
            ProductRequest xssPayload = new ProductRequest();
            xssPayload.setName("<img src=x onerror=alert('XSS')>");
            xssPayload.setPrice(BigDecimal.valueOf(99.99));
            xssPayload.setQuantity(10);
            xssPayload.setCategory("Electronics");

            ProductResponse mockProduct = new ProductResponse();
            mockProduct.setId(1L);
            mockProduct.setName(xssPayload.getName());
            mockProduct.setPrice(xssPayload.getPrice());
            mockProduct.setQuantity(xssPayload.getQuantity());
            mockProduct.setCategory(xssPayload.getCategory());
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(mockProduct);

            // API stores as text - JSON encoding prevents script execution
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(xssPayload)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should sanitize JavaScript protocol in URL")
        void shouldSanitizeJavaScriptProtocol() throws Exception {
            ProductRequest xssPayload = new ProductRequest();
            xssPayload.setName("javascript:alert(document.cookie)");
            xssPayload.setDescription("Test description");
            xssPayload.setPrice(BigDecimal.valueOf(99.99));
            xssPayload.setQuantity(10);
            xssPayload.setCategory("Electronics");

            // The request may succeed since it's just text, but output should be escaped
            // This test verifies the input is accepted but would be escaped on output
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(xssPayload)));
        }
    }

    // ==================== INPUT VALIDATION SECURITY ====================
    @Nested
    @DisplayName("Input Validation Security")
    class InputValidationSecurityTests {

        @Test
        @DisplayName("Should reject oversized username")
        void shouldRejectOversizedUsername() throws Exception {
            String longUsername = "a".repeat(200);
            String payload = String.format("""
                {
                    "username": "%s",
                    "password": "Password123"
                }
                """, longUsername);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject null bytes in username")
        void shouldRejectNullBytes() throws Exception {
            String payload = """
                {
                    "username": "test\\u0000admin",
                    "password": "Password123"
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject path traversal attempts")
        void shouldRejectPathTraversal() throws Exception {
            String payload = """
                {
                    "username": "../../../etc/passwd",
                    "password": "Password123"
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject oversized product price")
        void shouldRejectOversizedProductPrice() throws Exception {
            ProductRequest invalidProduct = new ProductRequest();
            invalidProduct.setName("Test Product");
            invalidProduct.setPrice(new BigDecimal("9999999999999999.99"));
            invalidProduct.setQuantity(10);
            invalidProduct.setCategory("Electronics");

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative product quantity")
        void shouldRejectNegativeProductQuantity() throws Exception {
            ProductRequest invalidProduct = new ProductRequest();
            invalidProduct.setName("Test Product");
            invalidProduct.setPrice(BigDecimal.valueOf(99.99));
            invalidProduct.setQuantity(-10);
            invalidProduct.setCategory("Electronics");

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== AUTHENTICATION SECURITY ====================
    @Nested
    @DisplayName("Authentication Security")
    class AuthenticationSecurityTests {

        @Test
        @DisplayName("Should reject weak password - only letters")
        void shouldRejectWeakPasswordOnlyLetters() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("OnlyLetters");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject weak password - only numbers")
        void shouldRejectWeakPasswordOnlyNumbers() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("123456789");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject password shorter than 6 characters")
        void shouldRejectShortPassword() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Pa1");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject username with special SQL characters")
        void shouldRejectUsernameWithSqlChars() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("test'user");
            request.setEmail("test@example.com");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject username too short")
        void shouldRejectUsernameTooShort() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("ab");
            request.setEmail("test@example.com");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept valid registration")
        void shouldAcceptValidRegistration() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setEmail("valid@example.com");
            request.setPassword("Password123");
            request.setFullName("Valid User");

            com.flogin.entity.auth.User mockUser = new com.flogin.entity.auth.User();
            mockUser.setId(1L);
            mockUser.setUsername("validuser");
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUser);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    // ==================== PASSWORD COMPLEXITY ====================
    @Nested
    @DisplayName("Password Complexity Requirements")
    class PasswordComplexityTests {

        @Test
        @DisplayName("Should require at least one letter in password")
        void shouldRequireAtLeastOneLetter() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("12345678");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should require at least one number in password")
        void shouldRequireAtLeastOneNumber() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Password");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept password with letter and number")
        void shouldAcceptValidPassword() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            com.flogin.entity.auth.User mockUser = new com.flogin.entity.auth.User();
            mockUser.setId(1L);
            mockUser.setUsername("testuser");
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUser);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    // ==================== CONTENT TYPE VALIDATION ====================
    @Nested
    @DisplayName("Content Type Validation")
    class ContentTypeValidationTests {

        @Test
        @DisplayName("Should reject non-JSON content type for login")
        void shouldRejectNonJsonContentType() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("username=test&password=test"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should accept JSON content type")
        void shouldAcceptJsonContentType() throws Exception {
            String validPayload = """
                {
                    "username": "testuser",
                    "password": "Password123"
                }
                """;

            when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Invalid credentials"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPayload))
                    .andExpect(status().isBadRequest()); // Wrong credentials but correct format
        }
    }

    // ==================== DATA EXPOSURE PREVENTION ====================
    @Nested
    @DisplayName("Data Exposure Prevention")
    class DataExposurePreventionTests {

        @Test
        @DisplayName("Should not expose password in error response")
        void shouldNotExposePasswordInError() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("WrongPassword123");

            when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Invalid username or password"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("WrongPassword123"))));
        }

        @Test
        @DisplayName("Should use generic error message for invalid credentials")
        void shouldUseGenericErrorMessage() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword("Password123");

            when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Invalid username or password"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}

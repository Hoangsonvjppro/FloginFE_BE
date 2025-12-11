package com.flogin.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.controller.AuthController;
import com.flogin.dto.auth.LoginRequest;
import com.flogin.dto.auth.RegisterRequest;
import com.flogin.entity.auth.User;
import com.flogin.exception.BadRequestException;
import com.flogin.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho AuthController sử dụng @WebMvcTest.
 * 
 * Tests:
 * - POST /api/auth/login - Success, Failure
 * - POST /api/auth/register - Success, Failure
 * 
 * Note: addFilters = false để disable Spring Security filters
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Unit Tests")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("TC_CTRL_LOGIN_001: Login success -> 200 OK với user info")
        void login_WithValidCredentials_ShouldReturn200() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("Pass123");

            when(authService.login(any(LoginRequest.class))).thenReturn(testUser);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.fullName").value("Test User"));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("TC_CTRL_LOGIN_002: Login failure - Invalid credentials -> 400")
        void login_WithInvalidCredentials_ShouldReturn400() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BadRequestException("Invalid username or password"));

            // Act & Assert - expect exception to propagate (no @ControllerAdvice)
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC_CTRL_LOGIN_003: Missing username -> 400")
        void login_WithMissingUsername_ShouldReturn400() throws Exception {
            // Arrange - Request không có username
            String requestJson = "{\"password\":\"Pass123\"}";

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC_CTRL_LOGIN_004: Missing password -> 400")
        void login_WithMissingPassword_ShouldReturn400() throws Exception {
            // Arrange
            String requestJson = "{\"username\":\"testuser\"}";

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC_CTRL_LOGIN_005: Empty request body -> 400")
        void login_WithEmptyBody_ShouldReturn400() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("TC_CTRL_LOGIN_006: Username too short (< 3 chars) -> 400")
        void login_WithUsernameTooShort_ShouldReturn400() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("ab");
            request.setPassword("Pass123");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("TC_CTRL_LOGIN_007: Password too short (< 6 chars) -> 400")
        void login_WithPasswordTooShort_ShouldReturn400() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("Pa1");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("TC_CTRL_REG_001: Register success -> 201 Created")
        void register_WithValidData_ShouldReturn201() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("newuser@example.com");
            request.setPassword("Pass123");
            request.setFullName("New User");

            User newUser = new User();
            newUser.setId(2L);
            newUser.setUsername("newuser");
            newUser.setEmail("newuser@example.com");
            newUser.setFullName("New User");

            when(authService.register(any(RegisterRequest.class))).thenReturn(newUser);

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("User registered successfully"))
                    .andExpect(jsonPath("$.userId").value(2))
                    .andExpect(jsonPath("$.username").value("newuser"))
                    .andExpect(jsonPath("$.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.fullName").value("New User"));

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("TC_CTRL_REG_002: Username already exists -> 400")
        void register_WithExistingUsername_ShouldReturn400() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existinguser");
            request.setEmail("new@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BadRequestException("Username already exists"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC_CTRL_REG_003: Email already exists -> 400")
        void register_WithExistingEmail_ShouldReturn400() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("existing@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BadRequestException("Email already exists"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC_CTRL_REG_004: Username too short -> 400")
        void register_WithUsernameTooShort_ShouldReturn400() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("ab"); // < 3 chars
            request.setEmail("new@example.com");
            request.setPassword("Pass123");
            request.setFullName("Test User");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC_CTRL_REG_005: Password too short -> 400")
        void register_WithPasswordTooShort_ShouldReturn400() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("Pa1"); // < 6 chars
            request.setFullName("Test User");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("TC_CTRL_REG_006: Missing email -> 400")
        void register_WithMissingEmail_ShouldReturn400() throws Exception {
            String requestJson = "{\"username\":\"newuser\",\"password\":\"Pass123\",\"fullName\":\"Test User\"}";

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("TC_CTRL_REG_007: Missing fullName -> 400")
        void register_WithMissingFullName_ShouldReturn400() throws Exception {
            String requestJson = "{\"username\":\"newuser\",\"email\":\"test@example.com\",\"password\":\"Pass123\"}";

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("TC_CTRL_REG_008: Empty request body -> 400")
        void register_WithEmptyBody_ShouldReturn400() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}

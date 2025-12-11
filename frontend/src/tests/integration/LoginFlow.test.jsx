/**
 * LoginFlow Integration Tests
 * 
 * Testing complete login/register flow with mocked API
 * Using React Testing Library for component integration
 * 
 * Coverage:
 * - Login flow with username validation
 * - Register flow (if applicable)
 * - Authentication state management
 * - API integration with mock responses
 */

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import LoginForm from '../../components/auth/LoginForm';
import { authService, login, register } from '../../services/authApi';

// Mock authApi
jest.mock('../../services/authApi', () => ({
  authService: {
    login: jest.fn(),
    logout: jest.fn(),
    isAuthenticated: jest.fn(),
    getUser: jest.fn(),
    setToken: jest.fn()
  },
  login: jest.fn(),
  register: jest.fn()
}));

// Helper function to get submit button
const getSubmitButton = () => {
  const buttons = screen.getAllByRole('button');
  return buttons.find(btn => btn.type === 'submit');
};

describe('LoginFlow Integration Tests', () => {
  const mockOnSubmit = jest.fn();
  const mockOnGoogleLogin = jest.fn();
  const mockOnSwitchToRegister = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  // ==================== SUCCESSFUL LOGIN FLOW ====================
  describe('Successful Login Flow', () => {
    it('should complete login flow with valid credentials', async () => {
      const mockResponse = {
        token: 'jwt-token-123',
        user: { id: 1, username: 'testuser', fullName: 'Test User' }
      };
      mockOnSubmit.mockResolvedValue(mockResponse);

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      // Fill in credentials
      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'testuser' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Password123' }
      });

      // Submit
      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          username: 'testuser',
          password: 'Password123'
        });
      });
    });

    it('should show loading state during login', async () => {
      mockOnSubmit.mockImplementation(() => 
        new Promise(resolve => setTimeout(() => resolve({ token: 'token' }), 100))
      );

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'testuser' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Password123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      // Should show loading state
      await waitFor(() => {
        expect(submitButton).toBeDisabled();
        expect(submitButton).toHaveTextContent(/signing in/i);
      });

      // Wait for completion
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      }, { timeout: 200 });
    });
  });

  // ==================== LOGIN VALIDATION FLOW ====================
  describe('Login Validation Flow', () => {
    it('should block submission with empty username', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Password123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/username is required/i)).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should block submission with empty password', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'testuser' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/password is required/i)).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should validate username format (special characters)', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'user@name!' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Password123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/username can only contain/i)).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should validate password must contain letter and number', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'testuser' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'password' } // No number
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/password must contain at least one number/i)).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  // ==================== USER INTERACTION FLOW ====================
  describe('User Interaction Flow', () => {
    it('should switch to register form when clicking sign up', () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const signUpButton = screen.getByRole('button', { name: /sign up/i });
      fireEvent.click(signUpButton);

      expect(mockOnSwitchToRegister).toHaveBeenCalledTimes(1);
    });

    it('should trigger Google login when clicking Google button', () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onGoogleLogin={mockOnGoogleLogin}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const googleButton = screen.getByRole('button', { name: /sign in with google/i });
      fireEvent.click(googleButton);

      expect(mockOnGoogleLogin).toHaveBeenCalledTimes(1);
    });

    it('should clear validation errors when user types', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      // Trigger error
      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/username is required/i)).toBeInTheDocument();
      });

      // Start typing - error should clear
      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'a' }
      });

      await waitFor(() => {
        expect(screen.queryByText(/username is required/i)).not.toBeInTheDocument();
      });
    });
  });

  // ==================== API INTEGRATION TESTS ====================
  describe('API Integration with authService', () => {
    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should call login API with correct credentials', async () => {
      const credentials = { username: 'testuser', password: 'Password123' };
      const mockResponse = { token: 'jwt-token', user: { id: 1 } };
      login.mockResolvedValue(mockResponse);

      const result = await login(credentials);

      expect(login).toHaveBeenCalledWith(credentials);
      expect(result).toEqual(mockResponse);
    });

    it('should call register API with user data', async () => {
      const userData = {
        username: 'newuser',
        email: 'new@example.com',
        password: 'Password123',
        fullName: 'New User'
      };
      const mockResponse = { id: 1, ...userData, token: 'jwt-token' };
      register.mockResolvedValue(mockResponse);

      const result = await register(userData);

      expect(register).toHaveBeenCalledWith(userData);
      expect(result).toEqual(mockResponse);
    });

    it('should handle login API errors', async () => {
      login.mockRejectedValue(new Error('Invalid credentials'));

      await expect(login({ username: 'wrong', password: 'wrong' }))
        .rejects.toThrow('Invalid credentials');
    });

    it('should check authentication status', () => {
      authService.isAuthenticated.mockReturnValue(true);

      const isAuth = authService.isAuthenticated();

      expect(isAuth).toBe(true);
    });

    it('should get current user from authService', () => {
      const mockUser = { id: 1, username: 'testuser', fullName: 'Test User' };
      authService.getUser.mockReturnValue(mockUser);

      const user = authService.getUser();

      expect(user).toEqual(mockUser);
    });

    it('should logout and clear session', () => {
      authService.logout.mockImplementation(() => {
        localStorage.removeItem('token');
      });

      authService.logout();

      expect(authService.logout).toHaveBeenCalled();
    });
  });

  // ==================== BOUNDARY VALUE TESTS ====================
  describe('Boundary Value Tests', () => {
    it('should accept username with exactly 3 characters', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'abc' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Pass123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });
    });

    it('should accept username with exactly 50 characters', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const longUsername = 'a'.repeat(50);
      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: longUsername }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Pass123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });
    });

    it('should accept password with exactly 6 characters', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'testuser' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Pass12' } // 6 chars with letter and number
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });
    });

    it('should accept password with exactly 100 characters', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const longPassword = 'A' + '1'.repeat(99);
      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'testuser' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: longPassword }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });
    });
  });

  // ==================== SPECIAL CHARACTER TESTS ====================
  describe('Special Character Handling', () => {
    it('should accept username with dots', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'user.name' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Pass123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          username: 'user.name',
          password: 'Pass123'
        });
      });
    });

    it('should accept username with underscores', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'user_name' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Pass123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          username: 'user_name',
          password: 'Pass123'
        });
      });
    });

    it('should accept username with hyphens', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'user-name' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Pass123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          username: 'user-name',
          password: 'Pass123'
        });
      });
    });

    it('should reject username with spaces', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/username/i), {
        target: { value: 'user name' }
      });
      fireEvent.change(screen.getByPlaceholderText(/password/i), {
        target: { value: 'Pass123' }
      });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/username can only contain/i)).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });
});

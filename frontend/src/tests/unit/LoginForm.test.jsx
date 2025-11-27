/**
 * LoginForm Integration & Mock Tests
 * 
 * Testing LoginForm component with mocked authService
 * Using @testing-library/react and jest.mock
 */

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginForm from '../../components/auth/LoginForm';
import { authService } from '../../services/authApi';

// Mock authService module
jest.mock('../../services/authApi', () => ({
  authService: {
    login: jest.fn(),
    logout: jest.fn(),
    isAuthenticated: jest.fn(),
    getUser: jest.fn()
  }
}));

// Helper function to get submit button (avoid conflict with Google button)
const getSubmitButton = () => {
  const buttons = screen.getAllByRole('button');
  return buttons.find(btn => btn.type === 'submit');
};

describe('LoginForm Component - Integration & Mock Tests', () => {

  const mockOnSubmit = jest.fn();
  const mockOnGoogleLogin = jest.fn();
  const mockOnSwitchToRegister = jest.fn();

  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
    localStorage.clear();
  });

  // ==================== TEST 1: RENDERING ====================

  describe('Rendering', () => {
    it('should render login form with all elements', () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      // Check email input exists
      const emailInput = screen.getByPlaceholderText(/email address/i);
      expect(emailInput).toBeInTheDocument();
      expect(emailInput).toHaveAttribute('type', 'email');

      // Check password input exists
      const passwordInput = screen.getByPlaceholderText(/password/i);
      expect(passwordInput).toBeInTheDocument();
      expect(passwordInput).toHaveAttribute('type', 'password');

      // Check submit button exists
      const submitButton = getSubmitButton();
      expect(submitButton).toBeInTheDocument();
      expect(submitButton).toHaveAttribute('type', 'submit');

      // Check Google login button exists
      const googleButton = screen.getByRole('button', { name: /sign in with google/i });
      expect(googleButton).toBeInTheDocument();

      // Check title and subtitle
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();
      expect(screen.getByText(/sign in to continue/i)).toBeInTheDocument();
    });

    it('should render sign up link button', () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const signUpButton = screen.getByRole('button', { name: /sign up/i });
      expect(signUpButton).toBeInTheDocument();
    });
  });

  // ==================== TEST 2: VALIDATION ERRORS ====================

  describe('Validation Errors', () => {
    it('should show validation error when submitting empty form', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      // Click submit without filling form
      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      // Wait for validation errors to appear
      await waitFor(() => {
        expect(screen.getByText('Email is required')).toBeInTheDocument();
      });

      expect(screen.getByText('Password is required')).toBeInTheDocument();

      // Verify onSubmit was NOT called
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should show error for invalid email format', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);

      // Enter invalid email
      fireEvent.change(emailInput, { target: { value: 'invalid-email' } });
      fireEvent.change(passwordInput, { target: { value: 'Password123' } });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/please enter a valid email/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should show error for password less than 8 characters', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);

      fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'Pass12' } }); // Only 6 chars

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should clear error messages when user types', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      // Submit empty form to trigger errors
      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Email is required')).toBeInTheDocument();
      });

      // Type in email field
      const emailInput = screen.getByPlaceholderText(/email address/i);
      fireEvent.change(emailInput, { target: { value: 'test@example.com' } });

      // Error should be cleared
      await waitFor(() => {
        expect(screen.queryByText('Email is required')).not.toBeInTheDocument();
      });
    });
  });

  // ==================== TEST 3: MOCK LOGIN SUCCESS ====================

  describe('Login Success (Mock)', () => {
    it('should call onSubmit with correct data when form is valid', async () => {
      // Mock successful onSubmit
      mockOnSubmit.mockResolvedValue({
        token: 'fake-token',
        user: { email: 'test@example.com', fullName: 'Test User' }
      });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      // Fill in form
      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);

      fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'Password123' } });

      // Submit form
      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      // Wait for onSubmit to be called
      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledTimes(1);
      });

      // Verify onSubmit was called with correct data
      expect(mockOnSubmit).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'Password123'
      });
    });

    it('should disable submit button while loading', async () => {
      // Mock onSubmit with delay
      mockOnSubmit.mockImplementation(() =>
        new Promise(resolve => setTimeout(() => resolve({ token: 'token' }), 100))
      );

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);

      fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'Password123' } });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      // Button should be disabled and show loading text
      await waitFor(() => {
        expect(submitButton).toBeDisabled();
        expect(submitButton).toHaveTextContent(/signing in/i);
      });

      // Wait for loading to complete
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      }, { timeout: 200 });
    });
  });

  // ==================== TEST 4: MOCK LOGIN FAILURE ====================

  describe('Login Failure (Mock)', () => {
    it('should handle login failure gracefully', async () => {
      // Mock onSubmit to reject with error
      const errorMessage = 'Invalid credentials';
      mockOnSubmit.mockRejectedValue({
        response: {
          data: {
            message: errorMessage
          }
        }
      });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);

      fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'WrongPassword123' } });

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      // Verify onSubmit was called
      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledTimes(1);
      });

      // Note: Error display would be handled by parent component
      // Form component doesn't handle server errors directly
    });

    it('should re-enable button after failed login', async () => {
      mockOnSubmit.mockRejectedValue(new Error('Network error'));

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);
      const submitButton = getSubmitButton();

      fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'Password123' } });
      fireEvent.click(submitButton);

      // Wait for error and button to be re-enabled
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      }, { timeout: 500 });
    });
  });

  // ==================== ADDITIONAL TESTS ====================

  describe('User Interactions', () => {
    it('should call onSwitchToRegister when sign up button is clicked', () => {
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

    it('should call onGoogleLogin when Google button is clicked', () => {
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

    it('should submit form on Enter key press', async () => {
      mockOnSubmit.mockResolvedValue({ token: 'token' });

      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);

      fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'Password123' } });

      // Press Enter on password field
      fireEvent.keyPress(passwordInput, { key: 'Enter', code: 13, charCode: 13 });

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });
    });
  });

  describe('Input Field Behavior', () => {
    it('should accept and display user input correctly', () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const emailInput = screen.getByPlaceholderText(/email address/i);
      const passwordInput = screen.getByPlaceholderText(/password/i);

      fireEvent.change(emailInput, { target: { value: 'user@test.com' } });
      fireEvent.change(passwordInput, { target: { value: 'MyPassword123' } });

      expect(emailInput).toHaveValue('user@test.com');
      expect(passwordInput).toHaveValue('MyPassword123');
    });

    it('should apply error class to invalid fields', async () => {
      render(
        <LoginForm
          onSubmit={mockOnSubmit}
          onSwitchToRegister={mockOnSwitchToRegister}
        />
      );

      const submitButton = getSubmitButton();
      fireEvent.click(submitButton);

      await waitFor(() => {
        const emailInput = screen.getByPlaceholderText(/email address/i);
        expect(emailInput).toHaveClass('error');
      });
    });
  });
});
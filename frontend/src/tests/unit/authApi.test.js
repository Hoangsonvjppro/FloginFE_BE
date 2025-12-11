import { login, register, authService } from '../../services/authApi';
import httpClient from '../../services/httpClient';

jest.mock('../../services/httpClient');

// Mock localStorage
const mockLocalStorage = (() => {
  let store = {};
  return {
    getItem: jest.fn(key => store[key] || null),
    setItem: jest.fn((key, value) => { store[key] = value.toString(); }),
    removeItem: jest.fn(key => { delete store[key]; }),
    clear: jest.fn(() => { store = {}; })
  };
})();
Object.defineProperty(window, 'localStorage', { value: mockLocalStorage });

describe('authApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLocalStorage.clear();
  });

  // ==================== LOGIN FUNCTION ====================
  describe('login', () => {
    test('login should call POST /api/auth/login with credentials', async () => {
      const mockResponse = { data: { token: 'fake-token', user: {} } };
      httpClient.post.mockResolvedValue(mockResponse);

      const credentials = { username: 'testuser', password: 'Pass123' };
      const result = await login(credentials);

      expect(httpClient.post).toHaveBeenCalledWith('/auth/login', credentials);
      expect(result).toEqual(mockResponse.data);
    });

    test('login should handle error response', async () => {
      const errorMessage = 'Invalid credentials';
      httpClient.post.mockRejectedValue(new Error(errorMessage));

      const credentials = { username: 'testuser', password: 'wrongpass' };
      await expect(login(credentials)).rejects.toThrow(errorMessage);
    });
  });

  // ==================== REGISTER FUNCTION ====================
  describe('register', () => {
    test('register should call POST /api/auth/register with user data', async () => {
      const mockResponse = { data: { message: 'User registered successfully', userId: 1 } };
      httpClient.post.mockResolvedValue(mockResponse);

      const userData = {
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'Pass123',
        fullName: 'New User'
      };
      const result = await register(userData);

      expect(httpClient.post).toHaveBeenCalledWith('/auth/register', userData);
      expect(result).toEqual(mockResponse.data);
    });

    test('register should handle error response', async () => {
      const errorMessage = 'Username already exists';
      httpClient.post.mockRejectedValue(new Error(errorMessage));

      const userData = { username: 'existinguser', email: 'test@example.com', password: 'Pass123' };
      await expect(register(userData)).rejects.toThrow(errorMessage);
    });
  });

  // ==================== AUTH SERVICE ====================
  describe('authService', () => {
    describe('login', () => {
      test('should login and store token in localStorage', async () => {
        const mockResponse = { data: { token: 'jwt-token-123', user: { id: 1, email: 'test@example.com' } } };
        httpClient.post.mockResolvedValue(mockResponse);

        const result = await authService.login('test@example.com', 'Pass123');

        expect(httpClient.post).toHaveBeenCalledWith('/auth/login', { email: 'test@example.com', password: 'Pass123' });
        expect(mockLocalStorage.setItem).toHaveBeenCalledWith('token', 'jwt-token-123');
        expect(result).toEqual(mockResponse.data);
      });

      test('should store user in localStorage', async () => {
        const mockUser = { id: 1, email: 'test@example.com', fullName: 'Test User' };
        const mockResponse = { data: { token: 'token', user: mockUser } };
        httpClient.post.mockResolvedValue(mockResponse);

        await authService.login('test@example.com', 'Pass123');

        expect(mockLocalStorage.setItem).toHaveBeenCalledWith('user', JSON.stringify(mockUser));
      });
    });

    describe('register', () => {
      test('should register with email, password and fullName', async () => {
        const mockResponse = { data: { message: 'Success', userId: 2 } };
        httpClient.post.mockResolvedValue(mockResponse);

        const result = await authService.register('new@example.com', 'Pass123', 'New User');

        expect(httpClient.post).toHaveBeenCalledWith('/auth/register', {
          email: 'new@example.com',
          password: 'Pass123',
          fullName: 'New User'
        });
        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('logout', () => {
      test('should remove token and user from localStorage', () => {
        authService.logout();

        expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('token');
        expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('user');
      });
    });

    describe('isAuthenticated', () => {
      test('should return true when token exists', () => {
        mockLocalStorage.getItem.mockReturnValue('some-token');

        expect(authService.isAuthenticated()).toBe(true);
      });

      test('should return false when token does not exist', () => {
        mockLocalStorage.getItem.mockReturnValue(null);

        expect(authService.isAuthenticated()).toBe(false);
      });
    });

    describe('getUser', () => {
      test('should return parsed user from localStorage', () => {
        const mockUser = { id: 1, email: 'test@example.com' };
        mockLocalStorage.getItem.mockReturnValue(JSON.stringify(mockUser));

        expect(authService.getUser()).toEqual(mockUser);
      });

      test('should return null when user not in localStorage', () => {
        mockLocalStorage.getItem.mockReturnValue(null);

        expect(authService.getUser()).toBeNull();
      });
    });
  });
});
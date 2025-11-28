import { login } from '../../services/authApi';
import httpClient from '../../services/httpClient';

jest.mock('../../services/httpClient');

describe('authApi', () => {
  test('login should call POST /api/auth/login', async () => {
    const mockResponse = { data: { token: 'fake-token', user: {} } };
    httpClient.post.mockResolvedValue(mockResponse);

    const credentials = { email: 'test@example.com', password: 'password123' };
    const result = await login(credentials);

    expect(httpClient.post).toHaveBeenCalledWith('/auth/login', credentials);
    expect(result).toEqual(mockResponse.data);
  });
});
import httpClient from './httpClient';

export const login = async (credentials) => {
  const response = await httpClient.post('/auth/login', credentials);
  return response.data;
};

export const register = async (userData) => {
  const response = await httpClient.post('/auth/register', userData);
  return response.data;
};

export const authService = {
  login: async (email, password) => {
    const response = await httpClient.post('/auth/login', { email, password });
    const data = response.data;
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data.user || { email }));
    return data;
  },
  
  register: async (email, password, fullName) => {
    const response = await httpClient.post('/auth/register', { email, password, fullName });
    return response.data;
  },
  
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
  
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },
  
  getUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }
};
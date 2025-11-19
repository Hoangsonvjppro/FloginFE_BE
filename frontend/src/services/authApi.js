import httpClient from './httpClient';

export const login = async (credentials) => {
  const response = await httpClient.post('/auth/login', credentials);
  return response.data;
};

export const register = async (userData) => {
  const response = await httpClient.post('/auth/register', userData);
  return response.data;
};
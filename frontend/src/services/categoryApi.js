import httpClient from './httpClient';

export const getAllCategories = async () => {
  const response = await httpClient.get('/categories');
  return response.data;
};

export const getCategoryById = async (id) => {
  const response = await httpClient.get(`/categories/${id}`);
  return response.data;
};

export const createCategory = async (categoryData) => {
  const response = await httpClient.post('/categories', categoryData);
  return response.data;
};

export const updateCategory = async (id, categoryData) => {
  const response = await httpClient.put(`/categories/${id}`, categoryData);
  return response.data;
};

export const deleteCategory = async (id) => {
  await httpClient.delete(`/categories/${id}`);
};

import httpClient from './httpClient';

export const getAllProducts = async () => {
  const response = await httpClient.get('/products');
  return response.data;
};

export const getProductById = async (id) => {
  const response = await httpClient.get(`/products/${id}`);
  return response.data;
};

export const createProduct = async (productData) => {
  const response = await httpClient.post('/products', productData);
  return response.data;
};

export const updateProduct = async (id, productData) => {
  const response = await httpClient.put(`/products/${id}`, productData);
  return response.data;
};

export const deleteProduct = async (id) => {
  await httpClient.delete(`/products/${id}`);
};

export const searchProducts = async (keyword) => {
  const response = await httpClient.get(`/products/search?keyword=${encodeURIComponent(keyword)}`);
  return response.data;
};

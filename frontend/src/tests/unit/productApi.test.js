import {
  getAllProducts,
  getProductById,
  createProduct,
  updateProduct,
  deleteProduct,
  searchProducts
} from '../../services/productApi';
import httpClient from '../../services/httpClient';

jest.mock('../../services/httpClient');

describe('productApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ==================== GET ALL PRODUCTS ====================
  describe('getAllProducts', () => {
    test('should call GET /products and return data', async () => {
      const mockProducts = [
        { id: 1, name: 'Product 1', price: 99.99, quantity: 100, category: 'ELECTRONICS' },
        { id: 2, name: 'Product 2', price: 49.99, quantity: 50, category: 'CLOTHING' }
      ];
      httpClient.get.mockResolvedValue({ data: mockProducts });

      const result = await getAllProducts();

      expect(httpClient.get).toHaveBeenCalledWith('/products');
      expect(result).toEqual(mockProducts);
    });

    test('should handle empty list', async () => {
      httpClient.get.mockResolvedValue({ data: [] });

      const result = await getAllProducts();

      expect(result).toEqual([]);
    });

    test('should handle error', async () => {
      httpClient.get.mockRejectedValue(new Error('Network error'));

      await expect(getAllProducts()).rejects.toThrow('Network error');
    });
  });

  // ==================== GET PRODUCT BY ID ====================
  describe('getProductById', () => {
    test('should call GET /products/:id and return product', async () => {
      const mockProduct = { id: 1, name: 'Product 1', price: 99.99, quantity: 100, category: 'ELECTRONICS' };
      httpClient.get.mockResolvedValue({ data: mockProduct });

      const result = await getProductById(1);

      expect(httpClient.get).toHaveBeenCalledWith('/products/1');
      expect(result).toEqual(mockProduct);
    });

    test('should handle not found error', async () => {
      httpClient.get.mockRejectedValue(new Error('Product not found'));

      await expect(getProductById(999)).rejects.toThrow('Product not found');
    });
  });

  // ==================== CREATE PRODUCT ====================
  describe('createProduct', () => {
    test('should call POST /products with product data', async () => {
      const newProduct = { name: 'New Product', price: 99.99, quantity: 100, category: 'ELECTRONICS' };
      const createdProduct = { id: 1, ...newProduct };
      httpClient.post.mockResolvedValue({ data: createdProduct });

      const result = await createProduct(newProduct);

      expect(httpClient.post).toHaveBeenCalledWith('/products', newProduct);
      expect(result).toEqual(createdProduct);
    });

    test('should handle validation error', async () => {
      httpClient.post.mockRejectedValue(new Error('Validation failed'));

      const invalidProduct = { name: '', price: -1 };
      await expect(createProduct(invalidProduct)).rejects.toThrow('Validation failed');
    });
  });

  // ==================== UPDATE PRODUCT ====================
  describe('updateProduct', () => {
    test('should call PUT /products/:id with product data', async () => {
      const productId = 1;
      const updateData = { name: 'Updated Product', price: 149.99, quantity: 200, category: 'CLOTHING' };
      const updatedProduct = { id: productId, ...updateData };
      httpClient.put.mockResolvedValue({ data: updatedProduct });

      const result = await updateProduct(productId, updateData);

      expect(httpClient.put).toHaveBeenCalledWith('/products/1', updateData);
      expect(result).toEqual(updatedProduct);
    });

    test('should handle not found error', async () => {
      httpClient.put.mockRejectedValue(new Error('Product not found'));

      await expect(updateProduct(999, {})).rejects.toThrow('Product not found');
    });
  });

  // ==================== DELETE PRODUCT ====================
  describe('deleteProduct', () => {
    test('should call DELETE /products/:id', async () => {
      httpClient.delete.mockResolvedValue({});

      await deleteProduct(1);

      expect(httpClient.delete).toHaveBeenCalledWith('/products/1');
    });

    test('should handle not found error', async () => {
      httpClient.delete.mockRejectedValue(new Error('Product not found'));

      await expect(deleteProduct(999)).rejects.toThrow('Product not found');
    });
  });

  // ==================== SEARCH PRODUCTS ====================
  describe('searchProducts', () => {
    test('should call GET /products/search with keyword', async () => {
      const mockResults = [{ id: 1, name: 'iPhone', price: 999 }];
      httpClient.get.mockResolvedValue({ data: mockResults });

      const result = await searchProducts('iPhone');

      expect(httpClient.get).toHaveBeenCalledWith('/products/search?keyword=iPhone');
      expect(result).toEqual(mockResults);
    });

    test('should encode special characters in keyword', async () => {
      httpClient.get.mockResolvedValue({ data: [] });

      await searchProducts('test & keyword');

      expect(httpClient.get).toHaveBeenCalledWith('/products/search?keyword=test%20%26%20keyword');
    });

    test('should handle empty results', async () => {
      httpClient.get.mockResolvedValue({ data: [] });

      const result = await searchProducts('nonexistent');

      expect(result).toEqual([]);
    });
  });
});

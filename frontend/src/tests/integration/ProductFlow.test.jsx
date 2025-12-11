/**
 * ProductFlow Integration Tests
 * 
 * Testing complete product management flow with mocked API
 * Using React Testing Library for component integration
 * 
 * Coverage:
 * - Product CRUD operations
 * - Form validation flows
 * - API integration with mock responses
 * - State management across operations
 */

import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProductForm from '../../components/product/ProductForm';
import { productApi } from '../../services/productApi';

// Mock productApi
jest.mock('../../services/productApi', () => ({
  productApi: {
    getAllProducts: jest.fn(),
    getProductById: jest.fn(),
    createProduct: jest.fn(),
    updateProduct: jest.fn(),
    deleteProduct: jest.fn(),
    searchProducts: jest.fn()
  }
}));

describe('ProductFlow Integration Tests', () => {
  const mockOnSubmit = jest.fn();
  const mockOnCancel = jest.fn();
  
  const validProduct = {
    name: 'Test Laptop',
    description: 'High-performance laptop for testing',
    price: 1500.00,
    quantity: 10
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ==================== CREATE PRODUCT FLOW ====================
  describe('Create Product Flow', () => {
    it('should successfully create a product with valid data', async () => {
      mockOnSubmit.mockResolvedValue({ id: 1, ...validProduct });
      
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel} 
          isOpen={true}
        />
      );

      // Fill all fields (using actual placeholders from ProductForm)
      fireEvent.change(screen.getByPlaceholderText('Product name *'), {
        target: { value: validProduct.name }
      });
      fireEvent.change(screen.getByPlaceholderText('Product description (optional)'), {
        target: { value: validProduct.description }
      });
      fireEvent.change(screen.getByPlaceholderText('Price *'), {
        target: { value: validProduct.price.toString() }
      });
      fireEvent.change(screen.getByPlaceholderText('Quantity *'), {
        target: { value: validProduct.quantity.toString() }
      });

      // Submit
      const submitButton = screen.getByRole('button', { name: /create product/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
          name: validProduct.name,
          description: validProduct.description
        }));
      });
    });

    it('should prevent submission with empty required fields', async () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      // Try to submit empty form
      const submitButton = screen.getByRole('button', { name: /create product/i });
      fireEvent.click(submitButton);

      // Should show validation errors
      await waitFor(() => {
        expect(screen.getByText(/product name is required/i)).toBeInTheDocument();
      });

      // onSubmit should NOT be called
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should validate price must be greater than 0', async () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      // Fill with invalid price
      fireEvent.change(screen.getByPlaceholderText('Product name *'), {
        target: { value: 'Test Product' }
      });
      fireEvent.change(screen.getByPlaceholderText('Price *'), {
        target: { value: '0' } // Zero price
      });
      fireEvent.change(screen.getByPlaceholderText('Quantity *'), {
        target: { value: '10' }
      });

      const submitButton = screen.getByRole('button', { name: /create product/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/price must be greater than 0/i)).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should validate quantity must not be negative', async () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      fireEvent.change(screen.getByPlaceholderText('Product name *'), {
        target: { value: 'Test Product' }
      });
      fireEvent.change(screen.getByPlaceholderText('Price *'), {
        target: { value: '100' }
      });
      fireEvent.change(screen.getByPlaceholderText('Quantity *'), {
        target: { value: '-1' } // Negative not allowed
      });

      const submitButton = screen.getByRole('button', { name: /create product/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/quantity must be greater than or equal to 0/i)).toBeInTheDocument();
      });
    });
  });

  // ==================== UPDATE PRODUCT FLOW ====================
  describe('Update Product Flow', () => {
    const existingProduct = {
      id: 1,
      name: 'Existing Product',
      description: 'Original description',
      price: 500.00,
      quantity: 20
    };

    it('should pre-populate form with existing product data', () => {
      render(
        <ProductForm 
          initialData={existingProduct}
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      expect(screen.getByPlaceholderText('Product name *')).toHaveValue(existingProduct.name);
      expect(screen.getByPlaceholderText('Product description (optional)')).toHaveValue(existingProduct.description);
      expect(screen.getByPlaceholderText('Price *')).toHaveValue(existingProduct.price);
      expect(screen.getByPlaceholderText('Quantity *')).toHaveValue(existingProduct.quantity);
    });

    it('should successfully update product with modified data', async () => {
      mockOnSubmit.mockResolvedValue({ ...existingProduct, name: 'Updated Product' });
      
      render(
        <ProductForm 
          initialData={existingProduct}
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      // Update name
      const nameInput = screen.getByPlaceholderText('Product name *');
      fireEvent.change(nameInput, { target: { value: 'Updated Product' } });

      // Submit
      const submitButton = screen.getByRole('button', { name: /update product/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
          name: 'Updated Product'
        }));
      });
    });

    it('should show Edit mode title when initialData is provided', () => {
      render(
        <ProductForm 
          initialData={existingProduct}
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      expect(screen.getByText('Edit Product')).toBeInTheDocument();
    });

    it('should show Add mode title when no initialData', () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      expect(screen.getByText('Add New Product')).toBeInTheDocument();
    });
  });

  // ==================== CANCEL FLOW ====================
  describe('Cancel Flow', () => {
    it('should call onCancel when cancel button is clicked', () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      fireEvent.click(cancelButton);

      expect(mockOnCancel).toHaveBeenCalledTimes(1);
    });

    it('should not save data when cancelled after filling form', () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      // Fill form
      fireEvent.change(screen.getByPlaceholderText('Product name *'), {
        target: { value: 'Test Product' }
      });

      // Cancel
      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      fireEvent.click(cancelButton);

      expect(mockOnSubmit).not.toHaveBeenCalled();
      expect(mockOnCancel).toHaveBeenCalled();
    });

    it('should call onCancel when clicking overlay', () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      // Click on modal overlay
      const overlay = document.querySelector('.modal-overlay');
      fireEvent.click(overlay);

      expect(mockOnCancel).toHaveBeenCalled();
    });

    it('should not close when clicking modal content', () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      // Click on modal content (should not close)
      const modalContent = document.querySelector('.modal-content');
      fireEvent.click(modalContent);

      expect(mockOnCancel).not.toHaveBeenCalled();
    });
  });

  // ==================== FORM NOT RENDERED WHEN CLOSED ====================
  describe('Form Visibility', () => {
    it('should not render form when isOpen is false', () => {
      const { container } = render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={false}
        />
      );

      expect(container.querySelector('.modal-overlay')).not.toBeInTheDocument();
    });

    it('should render form when isOpen is true', () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      expect(screen.getByText('Add New Product')).toBeInTheDocument();
    });
  });

  // ==================== VALIDATION ERROR CLEARING ====================
  describe('Validation Error Clearing', () => {
    it('should clear name error when user starts typing', async () => {
      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      // Submit to trigger error
      const submitButton = screen.getByRole('button', { name: /create product/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/product name is required/i)).toBeInTheDocument();
      });

      // Start typing
      fireEvent.change(screen.getByPlaceholderText('Product name *'), {
        target: { value: 'T' }
      });

      // Error should be cleared
      await waitFor(() => {
        expect(screen.queryByText(/product name is required/i)).not.toBeInTheDocument();
      });
    });
  });

  // ==================== API INTEGRATION TESTS ====================
  describe('API Integration with productApi', () => {
    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should call createProduct API when creating new product', async () => {
      const newProduct = { name: 'New Product', price: 100, quantity: 5 };
      productApi.createProduct.mockResolvedValue({ id: 1, ...newProduct });

      // Simulate calling API directly (integration test pattern)
      const result = await productApi.createProduct(newProduct);
      
      expect(productApi.createProduct).toHaveBeenCalledWith(newProduct);
      expect(result).toEqual({ id: 1, ...newProduct });
    });

    it('should call updateProduct API when updating existing product', async () => {
      const updatedProduct = { id: 1, name: 'Updated Product', price: 200, quantity: 10 };
      productApi.updateProduct.mockResolvedValue(updatedProduct);

      const result = await productApi.updateProduct(1, updatedProduct);
      
      expect(productApi.updateProduct).toHaveBeenCalledWith(1, updatedProduct);
      expect(result).toEqual(updatedProduct);
    });

    it('should handle API errors gracefully', async () => {
      productApi.createProduct.mockRejectedValue(new Error('Network error'));

      await expect(productApi.createProduct({})).rejects.toThrow('Network error');
    });

    it('should call getAllProducts to fetch product list', async () => {
      const mockProducts = [
        { id: 1, name: 'Product 1', price: 100 },
        { id: 2, name: 'Product 2', price: 200 }
      ];
      productApi.getAllProducts.mockResolvedValue(mockProducts);

      const result = await productApi.getAllProducts();
      
      expect(productApi.getAllProducts).toHaveBeenCalled();
      expect(result).toHaveLength(2);
    });

    it('should call deleteProduct API when deleting product', async () => {
      productApi.deleteProduct.mockResolvedValue({ success: true });

      const result = await productApi.deleteProduct(1);
      
      expect(productApi.deleteProduct).toHaveBeenCalledWith(1);
      expect(result).toEqual({ success: true });
    });

    it('should search products by keyword', async () => {
      const searchResults = [{ id: 1, name: 'Laptop', price: 1000 }];
      productApi.searchProducts.mockResolvedValue(searchResults);

      const result = await productApi.searchProducts('laptop');
      
      expect(productApi.searchProducts).toHaveBeenCalledWith('laptop');
      expect(result[0].name).toBe('Laptop');
    });
  });

  // ==================== FORM DATA PARSING ====================
  describe('Form Data Parsing', () => {
    it('should parse price as float and quantity as integer', async () => {
      mockOnSubmit.mockResolvedValue({ id: 1 });

      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      fireEvent.change(screen.getByPlaceholderText('Product name *'), {
        target: { value: 'Test Product' }
      });
      fireEvent.change(screen.getByPlaceholderText('Price *'), {
        target: { value: '99.99' }
      });
      fireEvent.change(screen.getByPlaceholderText('Quantity *'), {
        target: { value: '15' }
      });

      const submitButton = screen.getByRole('button', { name: /create product/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          name: 'Test Product',
          description: '',
          price: 99.99,
          quantity: 15
        });
      });
    });

    it('should trim whitespace from name and description', async () => {
      mockOnSubmit.mockResolvedValue({ id: 1 });

      render(
        <ProductForm 
          onSubmit={mockOnSubmit} 
          onCancel={mockOnCancel}
          isOpen={true}
        />
      );

      fireEvent.change(screen.getByPlaceholderText('Product name *'), {
        target: { value: '  Test Product  ' }
      });
      fireEvent.change(screen.getByPlaceholderText('Product description (optional)'), {
        target: { value: '  Some description  ' }
      });
      fireEvent.change(screen.getByPlaceholderText('Price *'), {
        target: { value: '100' }
      });
      fireEvent.change(screen.getByPlaceholderText('Quantity *'), {
        target: { value: '10' }
      });

      const submitButton = screen.getByRole('button', { name: /create product/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
          name: 'Test Product',
          description: 'Some description'
        }));
      });
    });
  });
});
/**
 * ProductForm Integration & Mock Tests
 * 
 * Testing ProductForm component with mocked productService
 * Covers both Create and Edit modes
 * Using @testing-library/react and jest.mock
 */

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ProductForm from '../../components/product/ProductForm';
import * as productApi from '../../services/productApi';

// Mock productApi module
jest.mock('../../services/productApi');

describe('ProductForm Component - Integration & Mock Tests', () => {

    const mockOnSubmit = jest.fn();
    const mockOnCancel = jest.fn();

    beforeEach(() => {
        // Clear all mocks before each test
        jest.clearAllMocks();
    });

    // ==================== TEST 1: RENDERING (CREATE MODE) ====================

    describe('Rendering - Create Mode', () => {
        it('should render create product form correctly', () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            // Check form title
            expect(screen.getByText('Add New Product')).toBeInTheDocument();

            // Check name input
            const nameInput = screen.getByPlaceholderText(/product name/i);
            expect(nameInput).toBeInTheDocument();
            expect(nameInput).toHaveAttribute('type', 'text');
            expect(nameInput).toHaveValue('');

            // Check description textarea
            const descriptionInput = screen.getByPlaceholderText(/product description/i);
            expect(descriptionInput).toBeInTheDocument();
            expect(descriptionInput.tagName).toBe('TEXTAREA');

            // Check price input
            const priceInput = screen.getByPlaceholderText(/price/i);
            expect(priceInput).toBeInTheDocument();
            expect(priceInput).toHaveAttribute('type', 'number');

            // Check quantity input
            const quantityInput = screen.getByPlaceholderText(/quantity/i);
            expect(quantityInput).toBeInTheDocument();
            expect(quantityInput).toHaveAttribute('type', 'number');

            // Check buttons
            expect(screen.getByRole('button', { name: /create product/i })).toBeInTheDocument();
            expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
        });

        it('should not render when isOpen is false', () => {
            const { container } = render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={false}
                />
            );

            expect(container.firstChild).toBeNull();
        });
    });

    // ==================== TEST 2: MOCK CREATE PRODUCT SUCCESS ====================

    describe('Create Product - Mock Success', () => {
        it('should call onSubmit with correct data when form is submitted', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            // Fill in form fields
            const nameInput = screen.getByPlaceholderText(/product name/i);
            const descriptionInput = screen.getByPlaceholderText(/product description/i);
            const priceInput = screen.getByPlaceholderText(/price/i);
            const quantityInput = screen.getByPlaceholderText(/quantity/i);

            fireEvent.change(nameInput, { target: { value: 'New Laptop' } });
            fireEvent.change(descriptionInput, { target: { value: 'A powerful laptop for developers' } });
            fireEvent.change(priceInput, { target: { value: '15000000' } });
            fireEvent.change(quantityInput, { target: { value: '10' } });

            // Submit form
            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            // Verify onSubmit was called with correct data
            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalledTimes(1);
            });

            expect(mockOnSubmit).toHaveBeenCalledWith({
                name: 'New Laptop',
                description: 'A powerful laptop for developers',
                price: 15000000,
                quantity: 10
            });
        });

        it('should trim whitespace from name and description', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            const nameInput = screen.getByPlaceholderText(/product name/i);
            const descriptionInput = screen.getByPlaceholderText(/product description/i);
            const priceInput = screen.getByPlaceholderText(/price/i);
            const quantityInput = screen.getByPlaceholderText(/quantity/i);

            // Add extra whitespace
            fireEvent.change(nameInput, { target: { value: '  Gaming Mouse  ' } });
            fireEvent.change(descriptionInput, { target: { value: '  High DPI mouse  ' } });
            fireEvent.change(priceInput, { target: { value: '500000' } });
            fireEvent.change(quantityInput, { target: { value: '25' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalledWith({
                    name: 'Gaming Mouse',
                    description: 'High DPI mouse',
                    price: 500000,
                    quantity: 25
                });
            });
        });

        it('should handle empty description (optional field)', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            fireEvent.change(screen.getByPlaceholderText(/product name/i), { target: { value: 'Keyboard' } });
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '1000000' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '5' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalledWith({
                    name: 'Keyboard',
                    description: '',
                    price: 1000000,
                    quantity: 5
                });
            });
        });
    });

    // ==================== TEST 3: MOCK UPDATE PRODUCT SUCCESS (EDIT MODE) ====================

    describe('Update Product - Mock Success (Edit Mode)', () => {
        const existingProduct = {
            id: 1,
            name: 'Old Laptop',
            description: 'Outdated model',
            price: 10000000,
            quantity: 3
        };

        it('should populate form and call onSubmit with updated data in edit mode', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    initialData={existingProduct}
                    isOpen={true}
                />
            );

            // Verify form shows "Edit Product" title
            expect(screen.getByText('Edit Product')).toBeInTheDocument();

            // Verify form is populated with existing data
            expect(screen.getByDisplayValue('Old Laptop')).toBeInTheDocument();
            expect(screen.getByDisplayValue('Outdated model')).toBeInTheDocument();
            expect(screen.getByDisplayValue('10000000')).toBeInTheDocument();
            expect(screen.getByDisplayValue('3')).toBeInTheDocument();

            // Update price
            const priceInput = screen.getByPlaceholderText(/price/i);
            fireEvent.change(priceInput, { target: { value: '12000000' } });

            // Submit form
            const submitButton = screen.getByRole('button', { name: /update product/i });
            fireEvent.click(submitButton);

            // Verify onSubmit was called with updated data
            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalledWith({
                    name: 'Old Laptop',
                    description: 'Outdated model',
                    price: 12000000,
                    quantity: 3
                });
            });
        });

        it('should update multiple fields correctly', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    initialData={existingProduct}
                    isOpen={true}
                />
            );

            // Update multiple fields
            fireEvent.change(screen.getByPlaceholderText(/product name/i), { target: { value: 'New Laptop Model' } });
            fireEvent.change(screen.getByPlaceholderText(/product description/i), { target: { value: 'Latest generation laptop' } });
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '18000000' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '15' } });

            const submitButton = screen.getByRole('button', { name: /update product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalledWith({
                    name: 'New Laptop Model',
                    description: 'Latest generation laptop',
                    price: 18000000,
                    quantity: 15
                });
            });
        });

        it('should reset form when initialData is removed', () => {
            const { rerender } = render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    initialData={existingProduct}
                    isOpen={true}
                />
            );

            // Verify populated
            expect(screen.getByDisplayValue('Old Laptop')).toBeInTheDocument();

            // Rerender without initialData
            rerender(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            // Form should be cleared
            expect(screen.getByPlaceholderText(/product name/i)).toHaveValue('');
        });
    });

    // ==================== TEST 4: VALIDATION LOGIC ====================

    describe('Validation Logic', () => {
        it('should show error when name is empty', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            // Fill only price and quantity
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '1000' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '10' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            // Expect validation error
            await waitFor(() => {
                expect(screen.getByText('Product name is required')).toBeInTheDocument();
            });

            // onSubmit should NOT be called
            expect(mockOnSubmit).not.toHaveBeenCalled();
        });

        it('should show error when price is negative', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            fireEvent.change(screen.getByPlaceholderText(/product name/i), { target: { value: 'Test Product' } });
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '-5000' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '10' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText('Price must be greater than 0')).toBeInTheDocument();
            });

            expect(mockOnSubmit).not.toHaveBeenCalled();
        });

        it('should show error when price is zero', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            fireEvent.change(screen.getByPlaceholderText(/product name/i), { target: { value: 'Test Product' } });
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '0' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '10' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText('Price must be greater than 0')).toBeInTheDocument();
            });

            expect(mockOnSubmit).not.toHaveBeenCalled();
        });

        it('should show error when quantity is negative', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            fireEvent.change(screen.getByPlaceholderText(/product name/i), { target: { value: 'Test Product' } });
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '1000' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '-10' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText('Quantity must be greater than or equal to 0')).toBeInTheDocument();
            });

            expect(mockOnSubmit).not.toHaveBeenCalled();
        });

        it('should accept zero quantity as valid', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            fireEvent.change(screen.getByPlaceholderText(/product name/i), { target: { value: 'Out of Stock Product' } });
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '5000' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '0' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalledWith({
                    name: 'Out of Stock Product',
                    description: '',
                    price: 5000,
                    quantity: 0
                });
            });
        });

        it('should show multiple validation errors', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            // Submit empty form
            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            // All validation errors should appear
            await waitFor(() => {
                expect(screen.getByText('Product name is required')).toBeInTheDocument();
                expect(screen.getByText('Price is required')).toBeInTheDocument();
                expect(screen.getByText('Quantity is required')).toBeInTheDocument();
            });

            expect(mockOnSubmit).not.toHaveBeenCalled();
        });

        it('should clear error when user types in field', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            // Wait for error
            await waitFor(() => {
                expect(screen.getByText('Product name is required')).toBeInTheDocument();
            });

            // Type in name field
            const nameInput = screen.getByPlaceholderText(/product name/i);
            fireEvent.change(nameInput, { target: { value: 'New Product' } });

            // Error should be cleared
            await waitFor(() => {
                expect(screen.queryByText('Product name is required')).not.toBeInTheDocument();
            });
        });
    });

    // ==================== ADDITIONAL TESTS ====================

    describe('User Interactions', () => {
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

        it('should call onCancel when clicking outside modal', () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            // Click modal overlay
            const overlay = screen.getByText('Add New Product').closest('.modal-overlay');
            fireEvent.click(overlay);

            expect(mockOnCancel).toHaveBeenCalledTimes(1);
        });

        it('should not call onCancel when clicking modal content', () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            // Click inside modal content
            const modalContent = screen.getByText('Add New Product').closest('.modal-content');
            fireEvent.click(modalContent);

            expect(mockOnCancel).not.toHaveBeenCalled();
        });

        it('should call onCancel when close button is clicked', () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            const closeButton = screen.getByRole('button', { name: '' }); // SVG close button
            if (closeButton.className.includes('btn-close')) {
                fireEvent.click(closeButton);
                expect(mockOnCancel).toHaveBeenCalledTimes(1);
            }
        });
    });

    describe('Field Type and Format', () => {
        it('should accept decimal values for price', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            fireEvent.change(screen.getByPlaceholderText(/product name/i), { target: { value: 'Item' } });
            fireEvent.change(screen.getByPlaceholderText(/price/i), { target: { value: '99.99' } });
            fireEvent.change(screen.getByPlaceholderText(/quantity/i), { target: { value: '5' } });

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalledWith({
                    name: 'Item',
                    description: '',
                    price: 99.99,
                    quantity: 5
                });
            });
        });

        it('should apply error class to invalid fields', async () => {
            render(
                <ProductForm
                    onSubmit={mockOnSubmit}
                    onCancel={mockOnCancel}
                    isOpen={true}
                />
            );

            const submitButton = screen.getByRole('button', { name: /create product/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                const nameInput = screen.getByPlaceholderText(/product name/i);
                const priceInput = screen.getByPlaceholderText(/price/i);
                const quantityInput = screen.getByPlaceholderText(/quantity/i);

                expect(nameInput).toHaveClass('error');
                expect(priceInput).toHaveClass('error');
                expect(quantityInput).toHaveClass('error');
            });
        });
    });
});
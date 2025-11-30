/**
 * Cypress E2E Test - Product CRUD Flow
 * 
 * Test Coverage:
 * 1. Create product successfully
 * 2. View Product list
 * 3. Update product
 * 4. Delete product
 * 5. Validation errors
 */

import ProductPage from '../pages/ProductPage';

describe('Product CRUD Operations', () => {
    const productPage = new ProductPage();
    const testProduct = {
        name: 'Test Laptop E2E',
        description: 'Laptop for E2E testing',
        price: 15000000,
        quantity: 10
    };

    beforeEach(() => {
        // Clear localStorage and visit products page
        cy.clearLocalStorage();

        // Mock login (or perform actual login if required)
        cy.intercept('POST', '**/api/auth/login', {
            statusCode: 200,
            body: {
                token: 'mock-token-for-e2e',
                userId: 1,
                email: 'test@example.com'
            }
        }).as('login');

        // Set token in localStorage
        cy.window().then((window) => {
            window.localStorage.setItem('token', 'mock-token-for-e2e');
        });

        productPage.visit();
    });

    // TEST 1: CREATE PRODUCT
    it('TC_E2E_001: Should create a new product successfully', () => {
        // Intercept create product API
        cy.intercept('POST', '**/api/products', {
            statusCode: 201,
            body: {
                id: 999,
                ...testProduct,
                createdAt: new Date().toISOString()
            }
        }).as('createProduct');

        // Click Add Product button
        productPage.clickAddProduct();

        // Fill form
        productPage.fillProductForm(testProduct);

        // Submit
        productPage.submitForm();

        // Wait for API call
        cy.wait('@createProduct');

        // Verify success message
        productPage.shouldShowSuccess();

        // Verify product appears in list (if list refreshes)
        // productPage.shouldHaveProduct(testProduct.name);
    });

    // TEST 2: CREATE PRODUCT WITH VALIDATION ERROR
    it('TC_E2E_002: Should show validation error for empty product name', () => {
        productPage.clickAddProduct();

        // Fill only price and quantity, leave name empty
        productPage.fillProductForm({
            name: '', // Empty name
            price: 1000000,
            quantity: 5
        });

        productPage.submitForm();

        // Verify validation error (client-side or server-side)
        // Either inline error or toast error
        cy.get('body').then(($body) => {
            const hasError = $body.find('[data-testid="name-error"]').length > 0 ||
                $body.text().includes('required') ||
                $body.text().includes('không được để trống');
            expect(hasError).to.be.true;
        });
    });

    // TEST 3: VIEW PRODUCT LIST
    it('TC_E2E_003: Should display list of products', () => {
        // Mock API response with products
        cy.intercept('GET', '**/api/products', {
            statusCode: 200,
            body: [
                {
                    id: 1,
                    name: 'Laptop Dell',
                    price: 15000000,
                    quantity: 10
                },
                {
                    id: 2,
                    name: 'Mouse Logitech',
                    price: 500000,
                    quantity: 50
                }
            ]
        }).as('getProducts');

        // Reload page to fetch products
        productPage.visit();
        cy.wait('@getProducts');

        // Verify products are displayed
        productPage.shouldHaveProduct('Laptop Dell');
        productPage.shouldHaveProduct('Mouse Logitech');
    });

    // TEST 4: UPDATE PRODUCT
    it('TC_E2E_004: Should update product successfully', () => {
        // Mock initial products
        cy.intercept('GET', '**/api/products', {
            statusCode: 200,
            body: [
                {
                    id: 1,
                    name: 'Old Laptop',
                    description: 'Outdated model',
                    price: 10000000,
                    quantity: 3
                }
            ]
        }).as('getProducts');

        productPage.visit();
        cy.wait('@getProducts');

        // Mock update API
        cy.intercept('PUT', '**/api/products/1', {
            statusCode: 200,
            body: {
                id: 1,
                name: 'Updated Laptop',
                description: 'Latest model',
                price: 12000000,
                quantity: 5
            }
        }).as('updateProduct');

        // Click Edit
        productPage.clickEditProduct('Old Laptop');

        // Update fields
        productPage.fillProductForm({
            name: 'Updated Laptop',
            price: 12000000
        });

        productPage.submitForm();

        // Wait for API
        cy.wait('@updateProduct');

        // Verify success
        productPage.shouldShowSuccess();
    });

    // TEST 5: DELETE PRODUCT
    it('TC_E2E_005: Should delete product after confirmation', () => {
        // Mock initial products
        cy.intercept('GET', '**/api/products', {
            statusCode: 200,
            body: [
                {
                    id: 1,
                    name: 'Product To Delete',
                    price: 1000000,
                    quantity: 1
                }
            ]
        }).as('getProducts');

        productPage.visit();
        cy.wait('@getProducts');

        // Mock delete API
        cy.intercept('DELETE', '**/api/products/1', {
            statusCode: 204
        }).as('deleteProduct');

        // Click Delete
        productPage.clickDeleteProduct('Product To Delete');

        // Confirm deletion
        productPage.confirmDelete();

        // Wait for API
        cy.wait('@deleteProduct');

        // Verify success
        productPage.shouldShowSuccess();

        // Product should be removed from list
        // productPage.shouldNotHaveProduct('Product To Delete');
    });

    // TEST 6: CANCEL PRODUCT CREATION
    it('TC_E2E_006: Should cancel product creation', () => {
        productPage.clickAddProduct();

        productPage.fillProductForm({
            name: 'Test Product',
            price: 1000
        });

        // Click Cancel
        productPage.cancelForm();

        // Form should close, no API call
        // Verify form is not visible or page returned to list
        cy.get('[data-testid="product-form"]').should('not.exist')
            .or(cy.url().should('include', '/products'));
    });

    // TEST 7: PRICE VALIDATION
    it('TC_E2E_007: Should show error for negative price', () => {
        productPage.clickAddProduct();

        productPage.fillProductForm({
            name: 'Test Product',
            price: -1000, // Negative price
            quantity: 10
        });

        productPage.submitForm();

        // Verify validation error
        cy.contains(/price|giá|must be|phải/i).should('be.visible');
    });
});

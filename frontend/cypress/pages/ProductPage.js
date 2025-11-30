/**
 * Product Page Object Model
 * 
 * This POM encapsulates all interactions with the Product management page
 * following the Page Object Model design pattern for cleaner E2E tests.
 */

class ProductPage {
    // Selectors
    get addProductButton() {
        return cy.get('[data-testid="add-product-btn"]')
            .or(cy.contains('button', /add.*product/i));
    }

    get productNameInput() {
        return cy.get('[data-testid="product-name"]')
            .or(cy.get('input[name="name"]'))
            .or(cy.get('input[placeholder*="name" i]'));
    }

    get productDescriptionInput() {
        return cy.get('[data-testid="product-description"]')
            .or(cy.get('textarea[name="description"]'))
            .or(cy.get('textarea[placeholder*="description" i]'));
    }

    get productPriceInput() {
        return cy.get('[data-testid="product-price"]')
            .or(cy.get('input[name="price"]'))
            .or(cy.get('input[type="number"][placeholder*="price" i]'));
    }

    get productQuantityInput() {
        return cy.get('[data-testid="product-quantity"]')
            .or(cy.get('input[name="quantity"]'))
            .or(cy.get('input[type="number"][placeholder*="quantity" i]'));
    }

    get submitButton() {
        return cy.get('[data-testid="submit-btn"]')
            .or(cy.contains('button', /create|save|submit/i));
    }

    get cancelButton() {
        return cy.get('[data-testid="cancel-btn"]')
            .or(cy.contains('button', /cancel/i));
    }

    get successMessage() {
        return cy.get('[data-testid="success-message"]')
            .or(cy.get('.toast-success'))
            .or(cy.get('.alert-success'))
            .or(cy.contains(/success|thành công/i));
    }

    get errorMessage() {
        return cy.get('[data-testid="error-message"]')
            .or(cy.get('.toast-error'))
            .or(cy.get('.alert-danger'))
            .or(cy.contains(/error|lỗi/i));
    }

    // Actions
    visit() {
        cy.visit('/products');
        return this;
    }

    clickAddProduct() {
        this.addProductButton.click();
        return this;
    }

    fillProductForm({ name, description, price, quantity }) {
        if (name) {
            this.productNameInput.clear().type(name);
        }
        if (description !== undefined) {
            this.productDescriptionInput.clear().type(description);
        }
        if (price) {
            this.productPriceInput.clear().type(price.toString());
        }
        if (quantity !== undefined) {
            this.productQuantityInput.clear().type(quantity.toString());
        }
        return this;
    }

    submitForm() {
        this.submitButton.click();
        return this;
    }

    cancelForm() {
        this.cancelButton.click();
        return this;
    }

    getProductInList(name) {
        return cy.contains('[data-testid="product-item"]', name)
            .or(cy.contains('tr', name))
            .or(cy.contains('.product-card', name));
    }

    clickEditProduct(name) {
        this.getProductInList(name).within(() => {
            cy.get('[data-testid="edit-btn"]')
                .or(cy.contains('button', /edit/i))
                .click();
        });
        return this;
    }

    clickDeleteProduct(name) {
        this.getProductInList(name).within(() => {
            cy.get('[data-testid="delete-btn"]')
                .or(cy.contains('button', /delete|xóa/i))
                .click();
        });
        return this;
    }

    confirmDelete() {
        cy.get('[data-testid="confirm-delete"]')
            .or(cy.contains('button', /confirm|delete|xóa/i))
            .click();
        return this;
    }

    // Assertions
    shouldShowSuccess(message) {
        if (message) {
            this.successMessage.should('be.visible').and('contain', message);
        } else {
            this.successMessage.should('be.visible');
        }
        return this;
    }

    shouldShowError(message) {
        if (message) {
            this.errorMessage.should('be.visible').and('contain', message);
        } else {
            this.errorMessage.should('be.visible');
        }
        return this;
    }

    shouldHaveProduct(name) {
        this.getProductInList(name).should('exist').and('be.visible');
        return this;
    }

    shouldNotHaveProduct(name) {
        this.getProductInList(name).should('not.exist');
        return this;
    }
}

export default ProductPage;

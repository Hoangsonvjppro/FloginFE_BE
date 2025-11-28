/**
 * Cypress E2E Test - Login Flow
 * 
 * Test Cases:
 * 1. Display login form correctly
 * 2. Login failure with wrong credentials
 * 3. Login success with correct credentials
 */

describe('Login Flow', () => {

    beforeEach(() => {
        // Visit login page before each test
        cy.visit('/login');

        // Clear localStorage to ensure clean state
        cy.clearLocalStorage();
    });

    // ==================== TEST CASE 1: DISPLAY LOGIN FORM ====================

    it('should display login form with all required elements', () => {
        // Check that email input is visible
        cy.get('input[type="email"]')
            .should('be.visible')
            .and('have.attr', 'placeholder');

        // Check that password input is visible
        cy.get('input[type="password"]')
            .should('be.visible')
            .and('have.attr', 'placeholder');

        // Check that login button is visible and enabled
        cy.get('button[type="submit"]')
            .should('be.visible')
            .and('not.be.disabled')
            .and('contain.text', /login|sign in/i);

        // Verify page title or heading
        cy.get('h1, h2, h3')
            .should('contain.text', /login|sign in/i);
    });

    // ==================== TEST CASE 2: LOGIN FAILURE ====================

    it('should display error message when login fails with wrong credentials', () => {
        // Enter invalid email
        cy.get('input[type="email"]')
            .type('wrong@example.com');

        // Enter invalid password
        cy.get('input[type="password"]')
            .type('wrongpassword');

        // Click submit button
        cy.get('button[type="submit"]')
            .click();

        // Wait for error message to appear
        cy.wait(1000);

        // Check for error message
        // Multiple selectors to handle different implementations
        cy.get('body').then(($body) => {
            const errorSelectors = [
                '.error',
                '.alert-danger',
                '.error-message',
                '[role="alert"]',
                '.notification',
                '.toast-error'
            ];

            let errorFound = false;

            for (const selector of errorSelectors) {
                if ($body.find(selector).length > 0) {
                    cy.get(selector)
                        .should('be.visible')
                        .and('contain.text', /invalid|error|incorrect|failed/i);
                    errorFound = true;
                    break;
                }
            }

            // If no specific error element, check for any text containing error keywords
            if (!errorFound) {
                cy.contains(/invalid|error|incorrect|failed|wrong/i)
                    .should('be.visible');
            }
        });

        // Verify user is still on login page (not redirected)
        cy.url().should('include', '/login');

        // Verify localStorage does NOT have token
        cy.window().then((window) => {
            expect(window.localStorage.getItem('token')).to.be.null;
        });
    });

    it('should display error for empty email field', () => {
        // Leave email empty, only fill password
        cy.get('input[type="password"]')
            .type('Test1234');

        // Try to submit
        cy.get('button[type="submit"]')
            .click();

        // Check for validation error
        cy.get('input[type="email"]').then(($input) => {
            // HTML5 validation or custom validation
            const isInvalid = $input.is(':invalid') ||
                $input.hasClass('invalid') ||
                $input.hasClass('error');
            expect(isInvalid).to.be.true;
        });
    });

    it('should display error for empty password field', () => {
        // Fill email, leave password empty
        cy.get('input[type="email"]')
            .type('test@example.com');

        // Try to submit
        cy.get('button[type="submit"]')
            .click();

        // Check for validation error
        cy.get('input[type="password"]').then(($input) => {
            const isInvalid = $input.is(':invalid') ||
                $input.hasClass('invalid') ||
                $input.hasClass('error');
            expect(isInvalid).to.be.true;
        });
    });

    // ==================== TEST CASE 3: LOGIN SUCCESS ====================

    it('should successfully login with correct credentials and redirect', () => {
        // Intercept the login API call (if using API)
        cy.intercept('POST', '**/api/auth/login', {
            statusCode: 200,
            body: {
                token: 'mock-jwt-token-123',
                userId: 1,
                email: 'test@example.com',
                fullName: 'Test User',
                message: 'Login successful'
            }
        }).as('loginRequest');

        // Enter valid email
        cy.get('input[type="email"]')
            .clear()
            .type('test@example.com');

        // Enter valid password
        cy.get('input[type="password"]')
            .clear()
            .type('Test1234');

        // Click submit button
        cy.get('button[type="submit"]')
            .click();

        // Wait for API call to complete
        cy.wait('@loginRequest');

        // Wait for redirect
        cy.wait(1000);

        // Verify redirect to home or dashboard
        cy.url().should('match', /\/(dashboard|home)?$/);

        // Verify localStorage has token
        cy.window().then((window) => {
            const token = window.localStorage.getItem('token');
            expect(token).to.not.be.null;
            expect(token).to.be.a('string');
            expect(token).to.have.length.greaterThan(0);
        });

        // Verify user is logged in (check for user menu, avatar, or logout button)
        cy.get('body').then(($body) => {
            const loggedInIndicators = [
                '[data-testid="user-menu"]',
                '[data-testid="logout-button"]',
                '.user-menu',
                '.avatar',
                'button:contains("Logout")',
                'button:contains("Sign Out")'
            ];

            let indicatorFound = false;

            for (const selector of loggedInIndicators) {
                if ($body.find(selector).length > 0) {
                    cy.get(selector).should('be.visible');
                    indicatorFound = true;
                    break;
                }
            }

            // If no specific indicator, just verify we're not on login page
            if (!indicatorFound) {
                cy.url().should('not.include', '/login');
            }
        });
    });

    // ==================== ADDITIONAL TEST CASES ====================

    it('should handle API errors gracefully', () => {
        // Intercept and mock server error
        cy.intercept('POST', '**/api/auth/login', {
            statusCode: 500,
            body: {
                message: 'Server error'
            }
        }).as('loginError');

        cy.get('input[type="email"]').type('test@example.com');
        cy.get('input[type="password"]').type('Test1234');
        cy.get('button[type="submit"]').click();

        cy.wait('@loginError');

        // Should show error message
        cy.contains(/error|failed|try again/i).should('be.visible');
    });

    it('should disable submit button while processing', () => {
        // Intercept with delay
        cy.intercept('POST', '**/api/auth/login', (req) => {
            req.reply({
                delay: 2000,
                statusCode: 200,
                body: { token: 'test-token' }
            });
        }).as('loginDelay');

        cy.get('input[type="email"]').type('test@example.com');
        cy.get('input[type="password"]').type('Test1234');

        cy.get('button[type="submit"]').click();

        // Button should be disabled during processing
        cy.get('button[type="submit"]')
            .should('be.disabled')
            .or('have.attr', 'aria-busy', 'true');
    });

    it('should allow navigation to register page', () => {
        // Check for register link
        cy.get('a[href*="register"], a:contains("Register"), a:contains("Sign Up")')
            .should('be.visible')
            .click();

        // Should navigate to register page
        cy.url().should('include', '/register');
    });
});

/**
 * Cypress E2E Test - Login Flow
 * 
 * Updated for username-based login per assignment requirements:
 * - Username: 3-50 characters, pattern ^[a-zA-Z0-9._-]+$
 * - Password: 6-100 characters, must contain letter AND number
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
        // Check that username input is visible (text type for username)
        cy.get('input#username, input[placeholder*="sername"]')
            .should('be.visible')
            .and('have.attr', 'type', 'text');

        // Check that password input is visible
        cy.get('input[type="password"]')
            .should('be.visible')
            .and('have.attr', 'placeholder');

        // Check that login button is visible and enabled
        cy.get('button[type="submit"]')
            .should('be.visible')
            .and('not.be.disabled');

        // Verify page title or heading
        cy.get('h1, h2, h3')
            .should('contain.text', /login|sign in|welcome/i);
    });

    // ==================== TEST CASE 2: LOGIN FAILURE ====================

    it('should display error message when login fails with wrong credentials', () => {
        // Enter invalid username
        cy.get('input#username, input[placeholder*="sername"]')
            .type('wronguser');

        // Enter invalid password
        cy.get('input[type="password"]')
            .type('wrongpass123');

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

    it('should display error for empty username field', () => {
        // Leave username empty, only fill password
        cy.get('input[type="password"]')
            .type('Test1234');

        // Try to submit
        cy.get('button[type="submit"]')
            .click();

        // Check for validation error
        cy.get('input#username, input[placeholder*="sername"]').then(($input) => {
            // HTML5 validation or custom validation
            const isInvalid = $input.is(':invalid') ||
                $input.hasClass('invalid') ||
                $input.hasClass('error');
            expect(isInvalid).to.be.true;
        });
    });

    it('should display error for empty password field', () => {
        // Fill username, leave password empty
        cy.get('input#username, input[placeholder*="sername"]')
            .type('testuser');

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
                username: 'testuser',
                fullName: 'Test User',
                message: 'Login successful'
            }
        }).as('loginRequest');

        // Enter valid username
        cy.get('input#username, input[placeholder*="sername"]')
            .clear()
            .type('testuser');

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

        cy.get('input#username, input[placeholder*="sername"]').type('testuser');
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

        cy.get('input#username, input[placeholder*="sername"]').type('testuser');
        cy.get('input[type="password"]').type('Test1234');

        cy.get('button[type="submit"]').click();

        // Button should be disabled during processing
        cy.get('button[type="submit"]')
            .should('be.disabled')
            .or('have.attr', 'aria-busy', 'true');
    });

    it('should allow navigation to register page', () => {
        // Check for register link
        cy.get('a[href*="register"], a:contains("Register"), button:contains("Sign Up"), button:contains("Sign up")')
            .should('be.visible')
            .click();

        // Should navigate to register page
        cy.url().should('include', '/register');
    });

    // ==================== USERNAME VALIDATION TESTS ====================

    it('should reject username with less than 3 characters', () => {
        cy.get('input#username, input[placeholder*="sername"]')
            .type('ab');
        cy.get('input[type="password"]')
            .type('Test1234');
        cy.get('button[type="submit"]')
            .click();

        // Should show validation error
        cy.contains(/username must be at least 3 characters/i).should('be.visible');
    });

    it('should reject username with invalid characters', () => {
        cy.get('input#username, input[placeholder*="sername"]')
            .type('user@name!');
        cy.get('input[type="password"]')
            .type('Test1234');
        cy.get('button[type="submit"]')
            .click();

        // Should show validation error
        cy.contains(/username can only contain/i).should('be.visible');
    });

    it('should accept username with valid special characters (dots, hyphens, underscores)', () => {
        cy.intercept('POST', '**/api/auth/login', {
            statusCode: 200,
            body: { token: 'mock-token', username: 'test_user.name-1' }
        }).as('loginRequest');

        cy.get('input#username, input[placeholder*="sername"]')
            .type('test_user.name-1');
        cy.get('input[type="password"]')
            .type('Test1234');
        cy.get('button[type="submit"]')
            .click();

        cy.wait('@loginRequest');
    });

    // ==================== PASSWORD VALIDATION TESTS ====================

    it('should reject password with less than 6 characters', () => {
        cy.get('input#username, input[placeholder*="sername"]')
            .type('testuser');
        cy.get('input[type="password"]')
            .type('Pa1');
        cy.get('button[type="submit"]')
            .click();

        // Should show validation error
        cy.contains(/password must be at least 6 characters/i).should('be.visible');
    });

    it('should reject password without letter', () => {
        cy.get('input#username, input[placeholder*="sername"]')
            .type('testuser');
        cy.get('input[type="password"]')
            .type('123456');
        cy.get('button[type="submit"]')
            .click();

        // Should show validation error
        cy.contains(/password must contain at least one letter/i).should('be.visible');
    });

    it('should reject password without number', () => {
        cy.get('input#username, input[placeholder*="sername"]')
            .type('testuser');
        cy.get('input[type="password"]')
            .type('password');
        cy.get('button[type="submit"]')
            .click();

        // Should show validation error
        cy.contains(/password must contain at least one number/i).should('be.visible');
    });
});
